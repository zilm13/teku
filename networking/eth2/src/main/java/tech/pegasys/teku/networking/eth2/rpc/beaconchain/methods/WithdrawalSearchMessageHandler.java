/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.networking.eth2.rpc.beaconchain.methods;

import static tech.pegasys.teku.infrastructure.async.SafeFuture.COMPLETE;
import static tech.pegasys.teku.ssz.backing.tree.GIndexUtil.get_helper_indices;

import com.google.common.base.Throwables;
import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.datastructures.networking.libp2p.rpc.WithdrawalSearchRequestMessage;
import tech.pegasys.teku.datastructures.state.BeaconState;
import tech.pegasys.teku.datastructures.state.Withdrawal;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.networking.eth2.peers.Eth2Peer;
import tech.pegasys.teku.networking.eth2.rpc.core.PeerRequiredLocalMessageHandler;
import tech.pegasys.teku.networking.eth2.rpc.core.ResponseCallback;
import tech.pegasys.teku.networking.eth2.rpc.core.RpcException;
import tech.pegasys.teku.networking.p2p.rpc.StreamClosedException;
import tech.pegasys.teku.ssz.backing.tree.LeafNode;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.storage.client.CombinedChainDataClient;

public class WithdrawalSearchMessageHandler
    extends PeerRequiredLocalMessageHandler<
        WithdrawalSearchRequestMessage, WithdrawalSearchMessageHandler.WithdrawalWithProof> {
  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

  private final CombinedChainDataClient combinedChainDataClient;

  public WithdrawalSearchMessageHandler(final CombinedChainDataClient combinedChainDataClient) {
    this.combinedChainDataClient = combinedChainDataClient;
  }

  @Override
  public void onIncomingMessage(
      final Eth2Peer peer,
      final WithdrawalSearchRequestMessage message,
      final ResponseCallback<WithdrawalSearchMessageHandler.WithdrawalWithProof> callback) {
    LOG.trace(
        "Peer {} requested Withdrawal with pubkey hash {}", peer.getId(), message.getPubkeyHash());

    sendDataWithProof(message, callback)
        .finish(
            callback::completeSuccessfully,
            error -> {
              final Throwable rootCause = Throwables.getRootCause(error);
              if (rootCause instanceof RpcException) {
                LOG.trace("Rejecting withdrawal search request", error); // Keep full context
                callback.completeWithErrorResponse((RpcException) rootCause);
              } else {
                if (rootCause instanceof StreamClosedException
                    || rootCause instanceof ClosedChannelException) {
                  LOG.trace("Stream closed while sending withdrawal", error);
                } else {
                  LOG.error("Failed to process withdrawal search", error);
                }
                callback.completeWithUnexpectedError(error);
              }
            });
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  private SafeFuture<?> sendDataWithProof(
      final WithdrawalSearchRequestMessage message,
      final ResponseCallback<WithdrawalSearchMessageHandler.WithdrawalWithProof> callback) {
    Optional<Pair<Integer, Withdrawal>> found =
        combinedChainDataClient
            .getFinalizedState()
            .flatMap(
                state ->
                    IntStream.range(0, state.getWithdrawals().size())
                        .mapToObj(i -> Pair.of(i, state.getWithdrawals().get(i)))
                        .filter(
                            pair ->
                                pair.getRight().getPubkey_hash().equals(message.getPubkeyHash()))
                        .findFirst());
    if (found.isEmpty()) {
      return SafeFuture.COMPLETE.toVoid();
    }
    Withdrawal withdrawal = found.get().getRight();
    Bytes32 hashTreeRoot = withdrawal.hashTreeRoot();
    TreeNode stateTree = combinedChainDataClient.getFinalizedState().get().getBackingNode();
    long withdrawalIndex = BeaconState.SSZ_SCHEMA.get().getChildGeneralizedIndex(14);
    AtomicReference<Optional<Pair<Long, TreeNode>>> withdrawalNodeOptional =
        new AtomicReference<>(Optional.empty());
    stateTree.iterate(
        withdrawalIndex,
        withdrawalIndex,
        (node, generalizedIndex) -> {
          if (!(node instanceof LeafNode)) {
            return true;
          }
          LeafNode leaf = (LeafNode) node;
          if (!leaf.hashTreeRoot().equals(hashTreeRoot)) {
            return true;
          }
          withdrawalNodeOptional.set(Optional.of(Pair.of(generalizedIndex, leaf)));
          return false;
        });
    if (withdrawalNodeOptional.get().isEmpty()) {
      return SafeFuture.failedFuture(
          new RpcException.HistoricalDataUnavailableException(
              "Requested historical blocks are currently unavailable"));
    }
    Pair<Long, TreeNode> withdrawalNode = withdrawalNodeOptional.get().get();
    Bytes leaf = ((LeafNode) withdrawalNode.getRight()).getData();
    List<Bytes32> proof =
        get_helper_indices(Collections.singletonList(withdrawalNode.getLeft())).stream()
            .map(it -> stateTree.get(it).hashTreeRoot())
            .collect(Collectors.toList());
    Bytes32 root = stateTree.hashTreeRoot();
    Long index = withdrawalNode.getLeft();
    callback.respond(new WithdrawalWithProof(root, proof, index, leaf));
    return COMPLETE.toVoid();
  }

  static class WithdrawalWithProof {
    private Bytes32 root;
    private List<Bytes32> proof;
    private long index;
    private Bytes withdrawal;

    public WithdrawalWithProof(Bytes32 root, List<Bytes32> proof, long index, Bytes withdrawal) {
      this.root = root;
      this.proof = proof;
      this.index = index;
      this.withdrawal = withdrawal;
    }

    public Bytes32 getRoot() {
      return root;
    }

    public List<Bytes32> getProof() {
      return proof;
    }

    public long getIndex() {
      return index;
    }

    public Bytes getWithdrawal() {
      return withdrawal;
    }
  }
}
