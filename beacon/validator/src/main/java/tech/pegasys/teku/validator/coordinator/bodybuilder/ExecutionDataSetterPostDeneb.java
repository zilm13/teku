/*
 * Copyright Consensys Software Inc., 2024
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

package tech.pegasys.teku.validator.coordinator.bodybuilder;

import java.util.Optional;
import tech.pegasys.teku.ethereum.performance.trackers.BlockProductionPerformance;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.BeaconBlockBodyBuilder;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayloadContext;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayloadResult;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.BeaconState;
import tech.pegasys.teku.spec.executionlayer.ExecutionLayerBlockProductionManager;
import tech.pegasys.teku.spec.schemas.SchemaDefinitions;

public class ExecutionDataSetterPostDeneb implements ExecutionDataSetter {
  private final ExecutionLayerBlockProductionManager executionLayerBlockProductionManager;
  private final KzgCommitmentsSetter kzgCommitmentsSetter;

  public ExecutionDataSetterPostDeneb(
      final ExecutionLayerBlockProductionManager executionLayerBlockProductionManager) {
    this.executionLayerBlockProductionManager = executionLayerBlockProductionManager;
    this.kzgCommitmentsSetter = new KzgCommitmentsSetter();
  }

  public SafeFuture<Void> setExecutionData(
      final Optional<ExecutionPayloadContext> executionPayloadContext,
      final BeaconBlockBodyBuilder bodyMaker,
      final SchemaDefinitions schemaDefinitions,
      final BeaconState blockSlotState,
      boolean shouldTryBuilderFlow,
      boolean setUnblindedContentIfPossible,
      final BlockProductionPerformance blockProductionPerformance) {

    final ExecutionPayloadResult executionPayloadResult =
        executionLayerBlockProductionManager.initiateBlockAndBlobsProduction(
            // kzg commitments are supported: we should have already merged by now, so we
            // can safely assume we have an executionPayloadContext
            executionPayloadContext.orElseThrow(
                () -> new IllegalStateException("Cannot provide kzg commitments before The Merge")),
            blockSlotState,
            shouldTryBuilderFlow,
            blockProductionPerformance);

    return SafeFuture.allOf(
        setPayloadOrHeader(bodyMaker, setUnblindedContentIfPossible, executionPayloadResult),
        //  Also need to set KZG Commitments, we could do it only with executionPayloadResult
        kzgCommitmentsSetter.setKzgCommitments(
            bodyMaker, setUnblindedContentIfPossible, schemaDefinitions, executionPayloadResult));
  }

  private SafeFuture<Void> setPayloadOrHeader(
      final BeaconBlockBodyBuilder bodyMaker,
      final boolean setUnblindedPayloadIfPossible,
      final ExecutionPayloadResult executionPayloadResult) {

    if (executionPayloadResult.getExecutionPayloadFuture().isPresent()) {
      // local flow
      return executionPayloadResult
          .getExecutionPayloadFuture()
          .get()
          .thenAccept(bodyMaker::executionPayload);
    }

    // builder flow
    return executionPayloadResult
        .getHeaderWithFallbackDataFuture()
        .orElseThrow()
        .thenAccept(
            headerWithFallbackData -> {
              if (setUnblindedPayloadIfPossible
                  && headerWithFallbackData.getFallbackData().isPresent()) {
                bodyMaker.executionPayload(
                    headerWithFallbackData.getFallbackData().get().getExecutionPayload());
              } else {
                bodyMaker.executionPayloadHeader(
                    headerWithFallbackData.getExecutionPayloadHeader());
              }
            });
  }
}
