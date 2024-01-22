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
import tech.pegasys.teku.spec.datastructures.state.beaconstate.BeaconState;
import tech.pegasys.teku.spec.executionlayer.ExecutionLayerBlockProductionManager;
import tech.pegasys.teku.spec.schemas.SchemaDefinitions;

public class ExecutionDataSetterPostMerge implements ExecutionDataSetter {
  private final ExecutionLayerBlockProductionManager executionLayerBlockProductionManager;

  public ExecutionDataSetterPostMerge(
      final ExecutionLayerBlockProductionManager executionLayerBlockProductionManager) {
    this.executionLayerBlockProductionManager = executionLayerBlockProductionManager;
  }

  public SafeFuture<Void> setExecutionData(
      final Optional<ExecutionPayloadContext> executionPayloadContext,
      final BeaconBlockBodyBuilder bodyMaker,
      final SchemaDefinitions schemaDefinitions,
      final BeaconState blockSlotState,
      boolean shouldTryBuilderFlow,
      boolean setUnblindedContentIfPossible,
      final BlockProductionPerformance blockProductionPerformance) {

    if (shouldTryBuilderFlow) {
      return setPayloadHeader(
          setUnblindedContentIfPossible,
          bodyMaker,
          blockSlotState,
          executionPayloadContext,
          blockProductionPerformance);
    }

    return setPayload(
        bodyMaker, blockSlotState, executionPayloadContext, blockProductionPerformance);
  }

  private SafeFuture<Void> setPayload(
      final BeaconBlockBodyBuilder bodyMaker,
      final BeaconState blockSlotState,
      final Optional<ExecutionPayloadContext> executionPayloadContext,
      final BlockProductionPerformance blockProductionPerformance) {
    return executionLayerBlockProductionManager
        .initiateBlockProduction(
            executionPayloadContext.get(), blockSlotState, false, blockProductionPerformance)
        .getExecutionPayloadFuture()
        .orElseThrow()
        .thenAccept(bodyMaker::executionPayload);
  }

  private SafeFuture<Void> setPayloadHeader(
      final boolean setUnblindedPayloadIfPossible,
      final BeaconBlockBodyBuilder bodyMaker,
      final BeaconState blockSlotState,
      final Optional<ExecutionPayloadContext> executionPayloadContext,
      final BlockProductionPerformance blockProductionPerformance) {

    return executionLayerBlockProductionManager
        .initiateBlockProduction(
            executionPayloadContext.get(), blockSlotState, true, blockProductionPerformance)
        .getHeaderWithFallbackDataFuture()
        .orElseThrow()
        .thenAccept(
            headerWithFallbackData -> {
              if (setUnblindedPayloadIfPossible
                  && headerWithFallbackData.getFallbackData().isPresent()) {
                bodyMaker.executionPayload(
                    headerWithFallbackData.getFallbackData().get().getExecutionPayload());
                return;
              }
              bodyMaker.executionPayloadHeader(headerWithFallbackData.getExecutionPayloadHeader());
            });
  }
}
