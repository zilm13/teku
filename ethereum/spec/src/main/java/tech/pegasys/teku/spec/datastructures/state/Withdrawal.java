/*
 * Copyright 2021 ConsenSys AG.
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

package tech.pegasys.teku.spec.datastructures.state;

import static tech.pegasys.teku.ssz.schema.SszPrimitiveSchemas.BYTES32_SCHEMA;
import static tech.pegasys.teku.ssz.schema.SszPrimitiveSchemas.UINT64_SCHEMA;

import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.containers.Container4;
import tech.pegasys.teku.ssz.containers.ContainerSchema4;
import tech.pegasys.teku.ssz.primitive.SszBytes32;
import tech.pegasys.teku.ssz.primitive.SszUInt64;
import tech.pegasys.teku.ssz.tree.TreeNode;

public class Withdrawal
    extends Container4<Withdrawal, SszUInt64, SszBytes32, SszUInt64, SszUInt64> {

  public static final WithdrawalSchema SSZ_SCHEMA = new WithdrawalSchema();

  public Withdrawal(
      UInt64 validator_index,
      Bytes32 withdrawal_credentials,
      UInt64 withdrawn_epoch,
      UInt64 amount) {
    super(
        SSZ_SCHEMA,
        SszUInt64.of(validator_index),
        SszBytes32.of(withdrawal_credentials),
        SszUInt64.of(withdrawn_epoch),
        SszUInt64.of(amount));
  }

  public Withdrawal(WithdrawalSchema type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public Withdrawal() {
    super(SSZ_SCHEMA);
  }

  public UInt64 getValidator_index() {
    return getField0().get();
  }

  public Bytes32 getWithdrawal_credentials() {
    return getField1().get();
  }

  public UInt64 getWithdrawn_epoch() {
    return getField2().get();
  }

  public UInt64 getAmount() {
    return getField3().get();
  }

  public static class WithdrawalSchema
      extends ContainerSchema4<Withdrawal, SszUInt64, SszBytes32, SszUInt64, SszUInt64> {

    public WithdrawalSchema() {
      super(
          "Withdrawal",
          namedSchema("validator_index", UINT64_SCHEMA),
          namedSchema("withdrawal_credentials", BYTES32_SCHEMA),
          namedSchema("withdrawn_epoch", UINT64_SCHEMA),
          namedSchema("amount", UINT64_SCHEMA));
    }

    @Override
    public Withdrawal createFromBackingNode(TreeNode node) {
      return new Withdrawal(this, node);
    }
  }
}
