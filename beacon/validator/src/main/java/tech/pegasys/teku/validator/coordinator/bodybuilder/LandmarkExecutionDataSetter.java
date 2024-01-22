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

public class LandmarkExecutionDataSetter {
  private final ExecutionDataSetter executionDataSetterPreMerge;
  private final ExecutionDataSetter executionDataSetterPostMerge;
  private final ExecutionDataSetter executionDataSetterPostDeneb;

  public LandmarkExecutionDataSetter(
      final ExecutionLayerBlockProductionManager executionLayerBlockProductionManager) {
    this.executionDataSetterPreMerge = new ExecutionDataSetterPreMerge();
    this.executionDataSetterPostMerge =
        new ExecutionDataSetterPostMerge(executionLayerBlockProductionManager);
    this.executionDataSetterPostDeneb =
        new ExecutionDataSetterPostDeneb(executionLayerBlockProductionManager);
  }

  public SafeFuture<Void> setExecutionData(
      final Optional<ExecutionPayloadContext> executionPayloadContext,
      final BeaconBlockBodyBuilder bodyMaker,
      final Optional<Boolean> requestedBlinded,
      final SchemaDefinitions schemaDefinitions,
      final BeaconState blockSlotState,
      final BlockProductionPerformance blockProductionPerformance) {

    // if requestedBlinded has been specified, we strictly follow it otherwise, we should run
    // Builder
    // flow (blinded) only if we have a validator registration
    final boolean shouldTryBuilderFlow =
        requestedBlinded.orElseGet(
            () ->
                executionPayloadContext
                    .map(ExecutionPayloadContext::isValidatorRegistrationPresent)
                    .orElse(false));

    // we should try to return unblinded content only if we try the builder flow with no explicit
    // request
    final boolean setUnblindedContentIfPossible = requestedBlinded.isEmpty();

    // Pre-Deneb: Execution Payload / Execution Payload Header
    final ExecutionDataSetter requiredSetter;
    if (!bodyMaker.supportsKzgCommitments()) {
      if (executionPayloadContext.isEmpty()) {
        requiredSetter = executionDataSetterPreMerge;
      } else {
        requiredSetter = executionDataSetterPostMerge;
      }
    } else {
      requiredSetter = executionDataSetterPostDeneb;
    }

    return requiredSetter.setExecutionData(
        executionPayloadContext,
        bodyMaker,
        schemaDefinitions,
        blockSlotState,
        shouldTryBuilderFlow,
        setUnblindedContentIfPossible,
        blockProductionPerformance);
  }
}
