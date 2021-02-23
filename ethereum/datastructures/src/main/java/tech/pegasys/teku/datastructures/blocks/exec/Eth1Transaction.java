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

package tech.pegasys.teku.datastructures.blocks.exec;

import static tech.pegasys.teku.util.config.Constants.MAX_BYTES_PER_TRANSACTION_PAYLOAD;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.backing.SszList;
import tech.pegasys.teku.ssz.backing.SszVector;
import tech.pegasys.teku.ssz.backing.containers.Container9;
import tech.pegasys.teku.ssz.backing.containers.ContainerSchema9;
import tech.pegasys.teku.ssz.backing.schema.SszComplexSchemas;
import tech.pegasys.teku.ssz.backing.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.ssz.backing.schema.SszVectorSchema;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.view.SszPrimitives;
import tech.pegasys.teku.ssz.backing.view.SszUtils;

public class Eth1Transaction
    extends Container9<
        Eth1Transaction,
        SszPrimitives.SszUInt64,
        SszPrimitives.SszUInt256,
        SszPrimitives.SszUInt64,
        SszVector<SszPrimitives.SszByte>,
        SszPrimitives.SszUInt256,
        SszList<SszPrimitives.SszByte>,
        SszPrimitives.SszUInt256,
        SszPrimitives.SszUInt256,
        SszPrimitives.SszUInt256> {

  static final SszComplexSchemas.SszByteListSchema TRANSACTION_INPUT_SCHEMA =
      new SszComplexSchemas.SszByteListSchema(MAX_BYTES_PER_TRANSACTION_PAYLOAD);
  public static final Eth1TransactionSchema SSZ_SCHEMA = new Eth1TransactionSchema();

  public Eth1Transaction() {
    super(SSZ_SCHEMA);
  }

  public Eth1Transaction(Eth1TransactionSchema type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public Eth1Transaction(
      UInt64 nonce,
      UInt256 gas_price,
      UInt64 gas_limit,
      Bytes recipient,
      UInt256 value,
      Bytes input,
      UInt256 v,
      UInt256 r,
      UInt256 s) {
    super(
        SSZ_SCHEMA,
        new SszPrimitives.SszUInt64(nonce),
        new SszPrimitives.SszUInt256(gas_price),
        new SszPrimitives.SszUInt64(gas_limit),
        SszUtils.toSszByteVector(recipient),
        new SszPrimitives.SszUInt256(value),
        SszUtils.toSszByteList(TRANSACTION_INPUT_SCHEMA, input),
        new SszPrimitives.SszUInt256(v),
        new SszPrimitives.SszUInt256(r),
        new SszPrimitives.SszUInt256(s));
  }

  public UInt64 getNonce() {
    return getField0().get();
  }

  public UInt256 getGas_price() {
    return getField1().get();
  }

  public UInt64 getGas_limit() {
    return getField2().get();
  }

  public Bytes getRecipient() {
    return getField3().sszSerialize();
  }

  public UInt256 getValue() {
    return getField4().get();
  }

  public Bytes getInput() {
    return getField5().sszSerialize();
  }

  public UInt256 getV() {
    return getField6().get();
  }

  public UInt256 getR() {
    return getField7().get();
  }

  public UInt256 getS() {
    return getField8().get();
  }

  public static class Eth1TransactionSchema
      extends ContainerSchema9<
          Eth1Transaction,
          SszPrimitives.SszUInt64,
          SszPrimitives.SszUInt256,
          SszPrimitives.SszUInt64,
          SszVector<SszPrimitives.SszByte>,
          SszPrimitives.SszUInt256,
          SszList<SszPrimitives.SszByte>,
          SszPrimitives.SszUInt256,
          SszPrimitives.SszUInt256,
          SszPrimitives.SszUInt256> {

    public Eth1TransactionSchema() {
      super(
          "Eth1Transaction",
          namedSchema("nonce", SszPrimitiveSchemas.UINT64_SCHEMA),
          namedSchema("gas_price", SszPrimitiveSchemas.UINT256_SCHEMA),
          namedSchema("gas_limit", SszPrimitiveSchemas.UINT64_SCHEMA),
          namedSchema("recipient", SszVectorSchema.create(SszPrimitiveSchemas.BYTE_SCHEMA, 20)),
          namedSchema("value", SszPrimitiveSchemas.UINT256_SCHEMA),
          namedSchema("input", TRANSACTION_INPUT_SCHEMA),
          namedSchema("v", SszPrimitiveSchemas.UINT256_SCHEMA),
          namedSchema("r", SszPrimitiveSchemas.UINT256_SCHEMA),
          namedSchema("s", SszPrimitiveSchemas.UINT256_SCHEMA));
    }

    @Override
    public Eth1Transaction createFromBackingNode(TreeNode node) {
      return new Eth1Transaction(this, node);
    }
  }
}
