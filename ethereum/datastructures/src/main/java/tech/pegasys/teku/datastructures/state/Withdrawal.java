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

package tech.pegasys.teku.datastructures.state;

import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes4;
import tech.pegasys.teku.ssz.backing.containers.Container5;
import tech.pegasys.teku.ssz.backing.containers.ContainerSchema5;
import tech.pegasys.teku.ssz.backing.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.view.SszPrimitives;

public class Withdrawal
    extends Container5<
        Withdrawal,
        SszPrimitives.SszBytes32,
        SszPrimitives.SszBytes4,
        SszPrimitives.SszBytes32,
        SszPrimitives.SszUInt64,
        SszPrimitives.SszUInt64> {

  public static final WithdrawalSchema SSZ_SCHEMA = new WithdrawalSchema();

  public Withdrawal(
      Bytes32 pubkey_hash,
      Bytes4 withdrawal_target,
      Bytes32 withdrawal_credentials,
      UInt64 amount,
      UInt64 withdrawal_epoch) {
    super(
        SSZ_SCHEMA,
        new SszPrimitives.SszBytes32(pubkey_hash),
        new SszPrimitives.SszBytes4(withdrawal_target),
        new SszPrimitives.SszBytes32(withdrawal_credentials),
        new SszPrimitives.SszUInt64(amount),
        new SszPrimitives.SszUInt64(withdrawal_epoch));
  }

  public Withdrawal(WithdrawalSchema type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public Withdrawal() {
    super(SSZ_SCHEMA);
  }

  public Bytes32 getPubkey_hash() {
    return getField0().get();
  }

  public Bytes4 getWithdrawal_target() {
    return getField1().get();
  }

  public Bytes32 getWithdrawal_credentials() {
    return getField2().get();
  }

  public UInt64 getAmount() {
    return getField3().get();
  }

  public UInt64 getWithdrawal_epoch() {
    return getField4().get();
  }

  public static class WithdrawalSchema
      extends ContainerSchema5<
          Withdrawal,
          SszPrimitives.SszBytes32,
          SszPrimitives.SszBytes4,
          SszPrimitives.SszBytes32,
          SszPrimitives.SszUInt64,
          SszPrimitives.SszUInt64> {

    public WithdrawalSchema() {
      super(
          "Withdrawal",
          namedSchema("pubkey_hash", SszPrimitiveSchemas.BYTES32_SCHEMA),
          namedSchema("withdrawal_target", SszPrimitiveSchemas.BYTES4_SCHEMA),
          namedSchema("withdrawal_credentials", SszPrimitiveSchemas.BYTES32_SCHEMA),
          namedSchema("amount", SszPrimitiveSchemas.UINT64_SCHEMA),
          namedSchema("withdrawal_epoch", SszPrimitiveSchemas.UINT64_SCHEMA));
    }

    @Override
    public Withdrawal createFromBackingNode(TreeNode node) {
      return new Withdrawal(this, node);
    }
  }
}
