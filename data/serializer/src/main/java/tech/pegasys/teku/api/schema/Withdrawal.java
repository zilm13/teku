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

package tech.pegasys.teku.api.schema;

import static tech.pegasys.teku.api.schema.SchemaConstants.EXAMPLE_BYTES32;
import static tech.pegasys.teku.api.schema.SchemaConstants.EXAMPLE_UINT64;
import static tech.pegasys.teku.api.schema.SchemaConstants.PATTERN_BYTES32;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class Withdrawal {
  @Schema(type = "string", example = EXAMPLE_UINT64, description = "Validator index")
  public final UInt64 validator_index;

  @Schema(
      type = "string",
      pattern = PATTERN_BYTES32,
      example = EXAMPLE_BYTES32,
      description = "Withdrawal credentials")
  public final Bytes32 withdrawal_credentials;

  @Schema(type = "string", example = EXAMPLE_UINT64, description = "Epoch of withdrawal")
  public final UInt64 withdrawn_epoch;

  @Schema(type = "string", example = EXAMPLE_UINT64, description = "Withdrawal amount in Gwei")
  public final UInt64 amount;

  @JsonCreator
  public Withdrawal(
      @JsonProperty("validator_index") final UInt64 validator_index,
      @JsonProperty("withdrawal_credentials") final Bytes32 withdrawal_credentials,
      @JsonProperty("withdrawn_epoch") final UInt64 withdrawn_epoch,
      @JsonProperty("amount") final UInt64 amount) {
    this.validator_index = validator_index;
    this.withdrawal_credentials = withdrawal_credentials;
    this.withdrawn_epoch = withdrawn_epoch;
    this.amount = amount;
  }

  public Withdrawal(final tech.pegasys.teku.spec.datastructures.state.Withdrawal withdrawal) {
    this.validator_index = withdrawal.getValidator_index();
    this.withdrawal_credentials = withdrawal.getWithdrawal_credentials();
    this.withdrawn_epoch = withdrawal.getWithdrawn_epoch();
    this.amount = withdrawal.getAmount();
  }

  public tech.pegasys.teku.spec.datastructures.state.Withdrawal asInternalWithdrawal() {
    return new tech.pegasys.teku.spec.datastructures.state.Withdrawal(
        validator_index, withdrawal_credentials, withdrawn_epoch, amount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Withdrawal that = (Withdrawal) o;
    return Objects.equal(validator_index, that.validator_index)
        && Objects.equal(withdrawal_credentials, that.withdrawal_credentials)
        && Objects.equal(withdrawn_epoch, that.withdrawn_epoch)
        && Objects.equal(amount, that.amount);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(validator_index, withdrawal_credentials, withdrawn_epoch, amount);
  }

  @Override
  public String toString() {
    return "Withdrawal{"
        + "validator_index="
        + validator_index
        + ", withdrawal_credentials="
        + withdrawal_credentials
        + ", withdrawn_epoch="
        + withdrawn_epoch
        + ", amount="
        + amount
        + '}';
  }
}
