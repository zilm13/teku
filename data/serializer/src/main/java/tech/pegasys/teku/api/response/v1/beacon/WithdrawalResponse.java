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

package tech.pegasys.teku.api.response.v1.beacon;

import static tech.pegasys.teku.api.schema.SchemaConstants.EXAMPLE_BYTES32;
import static tech.pegasys.teku.api.schema.SchemaConstants.EXAMPLE_UINT64;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.api.schema.Withdrawal;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class WithdrawalResponse {
  @JsonProperty("beacon_block_root")
  @Schema(type = "string", example = EXAMPLE_BYTES32, description = "Beacon block root")
  public final Bytes32 beaconBlockRoot;

  @JsonProperty("slot")
  @Schema(
      type = "string",
      example = EXAMPLE_UINT64,
      description = "The slot at which the proof is valid")
  public final UInt64 slot;

  @JsonProperty("proof")
  @ArraySchema(schema = @Schema(type = "string", example = EXAMPLE_BYTES32))
  public final List<Bytes32> proof;

  @JsonProperty("index")
  @Schema(
      type = "string",
      example = EXAMPLE_UINT64,
      description = "Withdrawal generalized index (in tree)")
  public final UInt64 index;

  @JsonProperty("withdrawal")
  public final Withdrawal withdrawal;

  @JsonCreator
  public WithdrawalResponse(
      @JsonProperty("beacon_block_root") final Bytes32 beaconBlockRoot,
      @JsonProperty("slot") final UInt64 slot,
      @JsonProperty("proof") final List<Bytes32> proof,
      @JsonProperty("index") final UInt64 index,
      @JsonProperty("withdrawal") final Withdrawal withdrawal) {
    this.beaconBlockRoot = beaconBlockRoot;
    this.slot = slot;
    this.proof = proof;
    this.index = index;
    this.withdrawal = withdrawal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WithdrawalResponse that = (WithdrawalResponse) o;
    return Objects.equal(beaconBlockRoot, that.beaconBlockRoot)
        && Objects.equal(slot, that.slot)
        && Objects.equal(proof, that.proof)
        && Objects.equal(index, that.index)
        && Objects.equal(withdrawal, that.withdrawal);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(beaconBlockRoot, slot, proof, index, withdrawal);
  }

  @Override
  public String toString() {
    return "WithdrawalResponse{"
        + "beaconBlockRoot="
        + beaconBlockRoot.toHexString()
        + "slot="
        + slot
        + ", proof="
        + proof.stream().map(Bytes32::toHexString).collect(Collectors.joining(","))
        + ", index="
        + index
        + ", withdrawal="
        + withdrawal
        + '}';
  }
}
