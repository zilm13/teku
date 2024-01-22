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

import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.spec.datastructures.blobs.versions.deneb.BlobKzgCommitmentsSchema;
import tech.pegasys.teku.spec.datastructures.blocks.blockbody.BeaconBlockBodyBuilder;
import tech.pegasys.teku.spec.datastructures.execution.BlobsBundle;
import tech.pegasys.teku.spec.datastructures.execution.ExecutionPayloadResult;
import tech.pegasys.teku.spec.datastructures.type.SszKZGCommitment;
import tech.pegasys.teku.spec.schemas.SchemaDefinitions;
import tech.pegasys.teku.spec.schemas.SchemaDefinitionsDeneb;

public class KzgCommitmentsSetter {

  public KzgCommitmentsSetter() {}

  public SafeFuture<Void> setKzgCommitments(
      final BeaconBlockBodyBuilder bodyMaker,
      final boolean setUnblindedPayloadIfPossible,
      final SchemaDefinitions schemaDefinitions,
      final ExecutionPayloadResult executionPayloadResult) {
    final BlobKzgCommitmentsSchema blobKzgCommitmentsSchema =
        SchemaDefinitionsDeneb.required(schemaDefinitions).getBlobKzgCommitmentsSchema();

    if (executionPayloadResult.getExecutionPayloadFuture().isPresent()) {
      // local flow
      return getExecutionBlobsBundle(executionPayloadResult)
          .thenApply(blobKzgCommitmentsSchema::createFromBlobsBundle)
          .thenAccept(bodyMaker::blobKzgCommitments);
    }

    // builder flow
    return getBuilderBlobKzgCommitments(
            blobKzgCommitmentsSchema, executionPayloadResult, setUnblindedPayloadIfPossible)
        .thenAccept(bodyMaker::blobKzgCommitments);
  }

  private SafeFuture<SszList<SszKZGCommitment>> getBuilderBlobKzgCommitments(
      final BlobKzgCommitmentsSchema blobKzgCommitmentsSchema,
      final ExecutionPayloadResult executionPayloadResult,
      final boolean setUnblindedPayloadIfPossible) {

    return executionPayloadResult
        .getHeaderWithFallbackDataFuture()
        .orElseThrow()
        .thenApply(
            headerWithFallbackData -> {
              if (setUnblindedPayloadIfPossible
                  && headerWithFallbackData.getFallbackData().isPresent()) {
                return blobKzgCommitmentsSchema.createFromBlobsBundle(
                    headerWithFallbackData.getFallbackData().get().getBlobsBundle().orElseThrow());
              }
              return headerWithFallbackData
                  .getBlobKzgCommitments()
                  .orElseThrow(
                      () ->
                          new IllegalStateException(
                              "builder BlobKzgCommitments are not available"));
            });
  }

  private SafeFuture<BlobsBundle> getExecutionBlobsBundle(
      final ExecutionPayloadResult executionPayloadResult) {
    return executionPayloadResult
        .getBlobsBundleFuture()
        .orElseThrow(this::executionBlobsBundleIsNotAvailableException)
        .thenApply(
            blobsBundle ->
                blobsBundle.orElseThrow(this::executionBlobsBundleIsNotAvailableException));
  }

  private IllegalStateException executionBlobsBundleIsNotAvailableException() {
    return new IllegalStateException("execution BlobsBundle is not available");
  }
}
