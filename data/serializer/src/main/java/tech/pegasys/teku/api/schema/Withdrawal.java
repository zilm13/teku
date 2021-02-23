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
import static tech.pegasys.teku.api.schema.SchemaConstants.EXAMPLE_BYTES4;
import static tech.pegasys.teku.api.schema.SchemaConstants.EXAMPLE_UINT64;
import static tech.pegasys.teku.api.schema.SchemaConstants.PATTERN_BYTES32;
import static tech.pegasys.teku.api.schema.SchemaConstants.PATTERN_BYTES4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes4;

public class Withdrawal {
  @Schema(
      type = "string",
      pattern = PATTERN_BYTES32,
      example = EXAMPLE_BYTES32,
      description = "Keccak hash of validator public key")
  public final Bytes32 pubkey_hash;

  @Schema(
      type = "string",
      pattern = PATTERN_BYTES4,
      example = EXAMPLE_BYTES4,
      description = "Keccak hash of validator public key")
  public final Bytes4 withdrawal_target;

  @Schema(
      type = "string",
      pattern = PATTERN_BYTES32,
      example = EXAMPLE_BYTES32,
      description = "Withdrawal credentials")
  public final Bytes32 withdrawal_credentials;

  @Schema(type = "string", example = EXAMPLE_UINT64, description = "Withdrawal amount in Gwei")
  public final UInt64 amount;

  @Schema(type = "string", example = EXAMPLE_UINT64, description = "Withdrawal epoch")
  public final UInt64 withdrawal_epoch;

  @JsonCreator
  public Withdrawal(
      @JsonProperty("pubkey_hash") final Bytes32 pubkey_hash,
      @JsonProperty("withdrawal_target") final Bytes4 withdrawal_target,
      @JsonProperty("withdrawal_credentials") final Bytes32 withdrawal_credentials,
      @JsonProperty("amount") final UInt64 amount,
      @JsonProperty("withdrawal_epoch") final UInt64 withdrawal_epoch) {
    this.pubkey_hash = pubkey_hash;
    this.withdrawal_target = withdrawal_target;
    this.withdrawal_credentials = withdrawal_credentials;
    this.amount = amount;
    this.withdrawal_epoch = withdrawal_epoch;
  }

  public Withdrawal(final tech.pegasys.teku.datastructures.state.Withdrawal withdrawal) {
    this.pubkey_hash = withdrawal.getPubkey_hash();
    this.withdrawal_target = withdrawal.getWithdrawal_target();
    this.withdrawal_credentials = withdrawal.getWithdrawal_credentials();
    this.amount = withdrawal.getAmount();
    this.withdrawal_epoch = withdrawal.getWithdrawal_epoch();
  }

  public tech.pegasys.teku.datastructures.state.Withdrawal asInternalWithdrawal() {
    return new tech.pegasys.teku.datastructures.state.Withdrawal(
        pubkey_hash, withdrawal_target, withdrawal_credentials, amount, withdrawal_epoch);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Withdrawal that = (Withdrawal) o;
    return Objects.equal(pubkey_hash, that.pubkey_hash)
        && Objects.equal(withdrawal_target, that.withdrawal_target)
        && Objects.equal(withdrawal_credentials, that.withdrawal_credentials)
        && Objects.equal(amount, that.amount)
        && Objects.equal(withdrawal_epoch, that.withdrawal_epoch);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        pubkey_hash, withdrawal_target, withdrawal_credentials, amount, withdrawal_epoch);
  }

  @Override
  public String toString() {
    return "Withdrawal{"
        + "pubkey_hash="
        + pubkey_hash
        + ", withdrawal_target="
        + withdrawal_target
        + ", withdrawal_credentials="
        + withdrawal_credentials
        + ", amount="
        + amount
        + ", withdrawal_epoch="
        + withdrawal_epoch
        + '}';
  }
}
