/*
 * Copyright ConsenSys Software Inc., 2023
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

import static com.google.common.base.Preconditions.checkNotNull;
import static tech.pegasys.teku.spec.config.Constants.MIN_EPOCHS_FOR_BLOB_SIDECARS_REQUESTS;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.Counter;
import org.hyperledger.besu.plugin.services.metrics.LabelledMetric;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.metrics.TekuMetricCategory;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.networking.eth2.peers.Eth2Peer;
import tech.pegasys.teku.networking.eth2.rpc.core.PeerRequiredLocalMessageHandler;
import tech.pegasys.teku.networking.eth2.rpc.core.ResponseCallback;
import tech.pegasys.teku.networking.eth2.rpc.core.RpcException;
import tech.pegasys.teku.networking.p2p.rpc.StreamClosedException;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.spec.datastructures.blocks.SlotAndBlockRoot;
import tech.pegasys.teku.spec.datastructures.execution.versions.deneb.BlobSidecar;
import tech.pegasys.teku.spec.datastructures.networking.libp2p.rpc.BlobSidecarsByRangeRequestMessage;
import tech.pegasys.teku.storage.client.CombinedChainDataClient;

public class BlobSidecarsByRangeMessageHandler
    extends PeerRequiredLocalMessageHandler<BlobSidecarsByRangeRequestMessage, BlobSidecar> {

  private static final Logger LOG = LogManager.getLogger();

  private final Spec spec;
  private final UInt64 denebForkEpoch;
  private final CombinedChainDataClient combinedChainDataClient;
  private final UInt64 maxRequestSize;
  private final UInt64 maxBlobsPerBlock;
  private final LabelledMetric<Counter> requestCounter;
  private final Counter totalBlobSidecarsRequestedCounter;

  public BlobSidecarsByRangeMessageHandler(
      final Spec spec,
      final UInt64 denebForkEpoch,
      final MetricsSystem metricsSystem,
      final CombinedChainDataClient combinedChainDataClient,
      final UInt64 maxRequestSize,
      final UInt64 maxBlobsPerBlock) {
    this.spec = spec;
    this.denebForkEpoch = denebForkEpoch;
    this.combinedChainDataClient = combinedChainDataClient;
    this.maxRequestSize = maxRequestSize;
    this.maxBlobsPerBlock = maxBlobsPerBlock;
    requestCounter =
        metricsSystem.createLabelledCounter(
            TekuMetricCategory.NETWORK,
            "rpc_blob_sidecars_by_range_requests_total",
            "Total number of blob sidecars by range requests received",
            "status");
    totalBlobSidecarsRequestedCounter =
        metricsSystem.createCounter(
            TekuMetricCategory.NETWORK,
            "rpc_blob_sidecars_by_range_requested_sidecars_total",
            "Total number of blob sidecars requested in accepted blob sidecars by range requests from peers");
  }

  @Override
  public void onIncomingMessage(
      final String protocolId,
      final Eth2Peer peer,
      final BlobSidecarsByRangeRequestMessage message,
      final ResponseCallback<BlobSidecar> callback) {
    final UInt64 startSlot = message.getStartSlot();
    LOG.trace(
        "Peer {} requested {} blob sidecars starting at slot {}.",
        peer.getId(),
        message.getCount(),
        startSlot);

    if (!peer.wantToMakeRequest()
        || !peer.wantToReceiveBlobSidecars(
            callback, maxRequestSize.min(message.getCount()).longValue())) {
      requestCounter.labels("rate_limited").inc();
      return;
    }
    requestCounter.labels("ok").inc();
    totalBlobSidecarsRequestedCounter.inc(message.getCount().longValue());

    final Bytes32 headBlockRoot =
        combinedChainDataClient
            .getBestBlockRoot()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Can't retrieve the block root chosen by fork choice."));

    combinedChainDataClient
        .getEarliestAvailableBlobSidecarEpoch()
        .thenCompose(
            earliestAvailableEpoch -> {
              final UInt64 requestEpoch = spec.computeEpochAtSlot(startSlot);
              if (checkRequestInMinEpochsRange(requestEpoch)
                  && !checkBlobSidecarsAreAvailable(earliestAvailableEpoch, requestEpoch)) {
                return SafeFuture.failedFuture(
                    new RpcException.ResourceUnavailableException(
                        "Requested blob sidecars are not available."));
              }
              final BlobSidecarsByRangeMessageHandler.RequestState initialState =
                  new BlobSidecarsByRangeMessageHandler.RequestState(
                      headBlockRoot, startSlot, message.getMaxSlot());
              if (!initialState.hasNext()) {
                return SafeFuture.completedFuture(initialState);
              } else {
                return sendBlobSidecars(initialState, callback);
              }
            })
        .finish(
            requestState -> {
              final int sentBlobSidecars = requestState.sentBlobSidecars.get();
              LOG.trace("Sent {} blob sidecars to peer {}.", sentBlobSidecars, peer.getId());
              callback.completeSuccessfully();
            },
            error -> handleProcessingRequestError(error, callback));
  }

  private boolean checkBlobSidecarsAreAvailable(
      final Optional<UInt64> earliestAvailableSidecarEpoch, final UInt64 requestEpoch) {
    return earliestAvailableSidecarEpoch
        .map(earliestEpoch -> earliestEpoch.isLessThanOrEqualTo(requestEpoch))
        .orElse(false);
  }

  private boolean checkRequestInMinEpochsRange(final UInt64 requestEpoch) {
    final UInt64 currentEpoch = combinedChainDataClient.getCurrentEpoch();
    final UInt64 minEpochForBlobsSidecar =
        denebForkEpoch.max(currentEpoch.minusMinZero(MIN_EPOCHS_FOR_BLOB_SIDECARS_REQUESTS));
    return requestEpoch.isGreaterThanOrEqualTo(minEpochForBlobsSidecar)
        && requestEpoch.isLessThanOrEqualTo(currentEpoch);
  }

  private SafeFuture<RequestState> sendBlobSidecars(
      final RequestState requestState, final ResponseCallback<BlobSidecar> callback) {
    return requestState
        .sendNextBlobSidecar(callback)
        .thenCompose(
            hasNext -> {
              if (hasNext) {
                return sendBlobSidecars(requestState, callback);
              } else {
                return SafeFuture.completedFuture(requestState);
              }
            });
  }

  private void handleProcessingRequestError(
      final Throwable error, final ResponseCallback<BlobSidecar> callback) {
    final Throwable rootCause = Throwables.getRootCause(error);
    if (rootCause instanceof RpcException) {
      LOG.trace("Rejecting blob sidecars by range request", error);
      callback.completeWithErrorResponse((RpcException) rootCause);
    } else {
      if (rootCause instanceof StreamClosedException
          || rootCause instanceof ClosedChannelException) {
        LOG.trace("Stream closed while sending requested blobs sidecars", error);
      } else {
        LOG.error("Failed to process blob sidecars request", error);
      }
      callback.completeWithUnexpectedError(error);
    }
  }

  @VisibleForTesting
  class RequestState {
    // MAX_BLOBS_PER_BLOCK for any possible fork
    private final Queue<BlobSidecarIdentifier> blobSidecarIdentifiers =
        new LinkedBlockingDeque<>(128);
    private final AtomicInteger sentBlobSidecars = new AtomicInteger(0);
    private final Bytes32 headBlockRoot;
    private final AtomicReference<UInt64> currentSlot;
    private final AtomicBoolean hasNext = new AtomicBoolean(true);
    private final UInt64 maxSlot;

    RequestState(final Bytes32 headBlockRoot, final UInt64 currentSlot, final UInt64 maxSlot) {
      this.headBlockRoot = headBlockRoot;
      this.currentSlot = new AtomicReference<>(currentSlot);
      this.maxSlot = maxSlot;
      if (currentSlot.isGreaterThan(maxSlot)) {
        hasNext.set(false);
      }
    }

    @VisibleForTesting
    UInt64 getCurrentSlot() {
      return currentSlot.get();
    }

    SafeFuture<Boolean> sendNextBlobSidecar(final ResponseCallback<BlobSidecar> callback) {
      if (!hasNext() || maxRequestSize.isLessThanOrEqualTo(sentBlobSidecars.get())) {
        return SafeFuture.completedFuture(false);
      }
      if (!blobSidecarIdentifiers.isEmpty()) {
        return sendBlobForNextBlobIdentifier(callback);
      } else {
        if (currentSlot.get().isGreaterThan(maxSlot)) {
          return toggleFinish();
        }
        return combinedChainDataClient
            .getBlockAtSlotExact(currentSlot.getAndUpdate(UInt64::increment), headBlockRoot)
            .thenAccept(
                block -> {
                  if (block.isEmpty()) {
                    hasNext.set(false);
                  } else {
                    populateQueue(block.get());
                  }
                })
            .thenCompose(__ -> sendBlobForNextBlobIdentifier(callback));
      }
    }

    private SafeFuture<Boolean> toggleFinish() {
      hasNext.set(false);
      return SafeFuture.completedFuture(false);
    }

    private SafeFuture<Boolean> sendBlobForNextBlobIdentifier(
        final ResponseCallback<BlobSidecar> callback) {
      if (blobSidecarIdentifiers.isEmpty()) {
        toggleFinish();
      }
      final BlobSidecarIdentifier blobSidecarIdentifier = blobSidecarIdentifiers.poll();
      checkNotNull(blobSidecarIdentifier, "blobSidecarIdentifier");
      return fetchBlobSidecar(blobSidecarIdentifier)
          .thenPeek(
              blobSidecarOptional -> {
                blobSidecarOptional.ifPresent(callback::respond);
              })
          .thenApply(
              __ -> {
                sentBlobSidecars.incrementAndGet();
                return hasNext();
              });
    }

    private SafeFuture<Optional<BlobSidecar>> fetchBlobSidecar(
        final BlobSidecarIdentifier blobSidecarIdentifier) {
      return combinedChainDataClient.getBlobSidecarBySlotIndexAndBlockRoot(
          blobSidecarIdentifier.getSlotAndBlockRoot().getSlot(),
          UInt64.valueOf(blobSidecarIdentifier.getIndex()),
          blobSidecarIdentifier.slotAndBlockRoot.getBlockRoot());
    }

    private void populateQueue(final SignedBeaconBlock block) {
      final int numberOfBlobs =
          block
              .getMessage()
              .getBody()
              .toVersionDeneb()
              .orElseThrow()
              .getBlobKzgCommitments()
              .size();
      for (int i = 0; i < numberOfBlobs; ++i) {
        blobSidecarIdentifiers.offer(
            new BlobSidecarIdentifier(new SlotAndBlockRoot(block.getSlot(), block.getRoot()), i));
      }
    }

    public boolean hasNext() {
      return hasNext.get();
    }

    class BlobSidecarIdentifier {
      private final SlotAndBlockRoot slotAndBlockRoot;
      private final int index;

      public BlobSidecarIdentifier(SlotAndBlockRoot slotAndBlockRoot, int index) {
        this.slotAndBlockRoot = slotAndBlockRoot;
        this.index = index;
      }

      public SlotAndBlockRoot getSlotAndBlockRoot() {
        return slotAndBlockRoot;
      }

      public int getIndex() {
        return index;
      }
    }
  }
}
