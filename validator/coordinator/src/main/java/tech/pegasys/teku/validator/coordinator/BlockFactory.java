/*
 * Copyright 2020 ConsenSys AG.
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

package tech.pegasys.teku.validator.coordinator;

import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_beacon_proposer_index;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_current_epoch;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_randao_mix;
import static tech.pegasys.teku.util.config.Constants.RECENT_BLOCK_ROOTS_SIZE;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.crypto.Hash;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.core.BlockProposalUtil;
import tech.pegasys.teku.core.Eth1EngineApiSchemaUtil;
import tech.pegasys.teku.core.ForkChoiceUtil;
import tech.pegasys.teku.core.StateTransition;
import tech.pegasys.teku.core.StateTransitionException;
import tech.pegasys.teku.core.exceptions.EpochProcessingException;
import tech.pegasys.teku.core.exceptions.SlotProcessingException;
import tech.pegasys.teku.datastructures.blocks.BeaconBlock;
import tech.pegasys.teku.datastructures.blocks.Eth1Data;
import tech.pegasys.teku.datastructures.blocks.exec.ExecutableData;
import tech.pegasys.teku.datastructures.operations.Attestation;
import tech.pegasys.teku.datastructures.operations.AttesterSlashing;
import tech.pegasys.teku.datastructures.operations.Deposit;
import tech.pegasys.teku.datastructures.operations.ProposerSlashing;
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit;
import tech.pegasys.teku.datastructures.state.BeaconState;
import tech.pegasys.teku.exec.eth1engine.Eth1EngineClient;
import tech.pegasys.teku.exec.eth1engine.Eth1EngineClient.Response;
import tech.pegasys.teku.exec.eth1engine.schema.ExecutableDataDTO;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.statetransition.OperationPool;
import tech.pegasys.teku.statetransition.attestation.AggregatingAttestationPool;
import tech.pegasys.teku.statetransition.attestation.AttestationForkChecker;
import tech.pegasys.teku.validator.coordinator.exceptions.Eth1BlockProductionException;

public class BlockFactory {
  private final BlockProposalUtil blockCreator;
  private final StateTransition stateTransition;
  private final AggregatingAttestationPool attestationPool;
  private final OperationPool<AttesterSlashing> attesterSlashingPool;
  private final OperationPool<ProposerSlashing> proposerSlashingPool;
  private final OperationPool<SignedVoluntaryExit> voluntaryExitPool;
  private final DepositProvider depositProvider;
  private final Eth1DataCache eth1DataCache;
  private final Bytes32 graffiti;
  private final Eth1EngineClient eth1EngineClient;

  public BlockFactory(
      final BlockProposalUtil blockCreator,
      final StateTransition stateTransition,
      final AggregatingAttestationPool attestationPool,
      final OperationPool<AttesterSlashing> attesterSlashingPool,
      final OperationPool<ProposerSlashing> proposerSlashingPool,
      final OperationPool<SignedVoluntaryExit> voluntaryExitPool,
      final DepositProvider depositProvider,
      final Eth1DataCache eth1DataCache,
      final Bytes32 graffiti,
      final Eth1EngineClient eth1EngineClient) {
    this.blockCreator = blockCreator;
    this.stateTransition = stateTransition;
    this.attestationPool = attestationPool;
    this.attesterSlashingPool = attesterSlashingPool;
    this.proposerSlashingPool = proposerSlashingPool;
    this.voluntaryExitPool = voluntaryExitPool;
    this.depositProvider = depositProvider;
    this.eth1DataCache = eth1DataCache;
    this.graffiti = graffiti;
    this.eth1EngineClient = eth1EngineClient;
  }

  public BeaconBlock createUnsignedBlock(
      final BeaconState previousState,
      final BeaconBlock previousBlock,
      final UInt64 newSlot,
      final BLSSignature randaoReveal,
      final Optional<Bytes32> optionalGraffiti)
      throws EpochProcessingException, SlotProcessingException, StateTransitionException,
          Eth1BlockProductionException {

    // Process empty slots up to the one before the new block slot
    final UInt64 slotBeforeBlock = newSlot.minus(UInt64.ONE);
    BeaconState blockPreState;
    if (previousState.getSlot().equals(slotBeforeBlock)) {
      blockPreState = previousState;
    } else {
      blockPreState = stateTransition.process_slots(previousState, slotBeforeBlock);
    }

    // Collect attestations to include
    final BeaconState blockSlotState = stateTransition.process_slots(blockPreState, newSlot);
    SSZList<Attestation> attestations =
        attestationPool.getAttestationsForBlock(
            blockSlotState, new AttestationForkChecker(blockSlotState));

    // Collect slashings to include
    final SSZList<ProposerSlashing> proposerSlashings =
        proposerSlashingPool.getItemsForBlock(blockSlotState);
    final SSZList<AttesterSlashing> attesterSlashings =
        attesterSlashingPool.getItemsForBlock(blockSlotState);

    // Collect exits to include
    final SSZList<SignedVoluntaryExit> voluntaryExits =
        voluntaryExitPool.getItemsForBlock(blockSlotState);

    // Collect deposits
    Eth1Data eth1Data = eth1DataCache.getEth1Vote(blockPreState);
    final SSZList<Deposit> deposits = depositProvider.getDeposits(blockPreState, eth1Data);

    final Bytes32 parentRoot = previousBlock.hash_tree_root();
    final Bytes32 eth1ParentHash = previousBlock.getBody().getExecutable_data().getBlock_hash();

    // Executable data
    UInt64 timestamp = ForkChoiceUtil.getSlotStartTime(newSlot, blockSlotState.getGenesis_time());
    UInt64 epoch = get_current_epoch(blockSlotState);
    // Pre-compute randao mix
    Bytes32 randaoMix =
        get_randao_mix(blockSlotState, epoch).xor(Hash.sha2_256(randaoReveal.toSSZBytes()));

    try {
      Response<ExecutableDataDTO> executableDataResponse =
          eth1EngineClient
              .eth2ProduceBlock(
                  eth1ParentHash,
                  randaoMix,
                  newSlot,
                  timestamp,
                  Collections.nCopies((int) RECENT_BLOCK_ROOTS_SIZE, Bytes32.ZERO))
              .get();
      if (executableDataResponse.getPayload() == null) {
        throw new IllegalStateException(
            "Failed to eth2_produceBlock(parent_hash="
                + eth1ParentHash
                + ", randaoMix="
                + randaoMix
                + ", slot="
                + newSlot
                + ", timestamp="
                + timestamp
                + "), reason: "
                + String.valueOf(executableDataResponse.getReason()));
      }

      ExecutableData executableData =
          Eth1EngineApiSchemaUtil.parseExecutableDataDTO(executableDataResponse.getPayload());

      return blockCreator
          .createNewUnsignedBlock(
              newSlot,
              get_beacon_proposer_index(blockPreState, newSlot),
              randaoReveal,
              blockSlotState,
              eth1ParentHash,
              parentRoot,
              eth1Data,
              executableData,
              optionalGraffiti.orElse(graffiti),
              attestations,
              proposerSlashings,
              attesterSlashings,
              deposits,
              voluntaryExits)
          .getBlock();
    } catch (InterruptedException | IllegalStateException | ExecutionException e) {
      throw new Eth1BlockProductionException(e);
    }
  }
}
