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

import static tech.pegasys.teku.util.config.Constants.BYTES_PER_LOGS_BLOOM;
import static tech.pegasys.teku.util.config.Constants.MAX_ETH1_TRANSACTIONS;

import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.backing.SszList;
import tech.pegasys.teku.ssz.backing.SszVector;
import tech.pegasys.teku.ssz.backing.containers.Container10;
import tech.pegasys.teku.ssz.backing.containers.ContainerSchema10;
import tech.pegasys.teku.ssz.backing.schema.SszListSchema;
import tech.pegasys.teku.ssz.backing.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.ssz.backing.schema.SszSchema;
import tech.pegasys.teku.ssz.backing.schema.SszVectorSchema;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.view.SszPrimitives;
import tech.pegasys.teku.ssz.backing.view.SszUtils;

public class ExecutableData
    extends Container10<
        ExecutableData,
        SszPrimitives.SszBytes32,
        SszPrimitives.SszBytes32,
        SszVector<SszPrimitives.SszByte>,
        SszPrimitives.SszBytes32,
        SszPrimitives.SszUInt64,
        SszPrimitives.SszUInt64,
        SszPrimitives.SszBytes32,
        SszVector<SszPrimitives.SszByte>,
        SszPrimitives.SszUInt64,
        SszList<Eth1Transaction>> {

  public static final ExecutableDataSchema SSZ_SCHEMA = new ExecutableDataSchema();

  public ExecutableData() {
    super(SSZ_SCHEMA);
  }

  public ExecutableData(ExecutableDataSchema type, TreeNode backingNode) {
    super(type, backingNode);
  }

  @Deprecated
  public ExecutableData(
      Bytes32 parent_hash,
      Bytes32 block_hash,
      Bytes coinbase,
      Bytes32 state_root,
      UInt64 gas_limit,
      UInt64 gas_used,
      Bytes32 receipt_root,
      Bytes logs_bloom,
      UInt64 difficulty,
      SSZList<Eth1Transaction> transactions) {
    super(
        SSZ_SCHEMA,
        new SszPrimitives.SszBytes32(parent_hash),
        new SszPrimitives.SszBytes32(block_hash),
        SszUtils.toSszByteVector(coinbase),
        new SszPrimitives.SszBytes32(state_root),
        new SszPrimitives.SszUInt64(gas_limit),
        new SszPrimitives.SszUInt64(gas_used),
        new SszPrimitives.SszBytes32(receipt_root),
        SszUtils.toSszByteVector(logs_bloom),
        new SszPrimitives.SszUInt64(difficulty),
        SszUtils.toSszList(SSZ_SCHEMA.getTransactionsSchema(), transactions));
  }

  public ExecutableData(
      Bytes32 parent_hash,
      Bytes32 block_hash,
      Bytes coinbase,
      Bytes32 state_root,
      UInt64 gas_limit,
      UInt64 gas_used,
      Bytes32 receipt_root,
      Bytes logs_bloom,
      UInt64 difficulty,
      List<Eth1Transaction> transactions) {
    super(
        SSZ_SCHEMA,
        new SszPrimitives.SszBytes32(parent_hash),
        new SszPrimitives.SszBytes32(block_hash),
        SszUtils.toSszByteVector(coinbase),
        new SszPrimitives.SszBytes32(state_root),
        new SszPrimitives.SszUInt64(gas_limit),
        new SszPrimitives.SszUInt64(gas_used),
        new SszPrimitives.SszBytes32(receipt_root),
        SszUtils.toSszByteVector(logs_bloom),
        new SszPrimitives.SszUInt64(difficulty),
        SszUtils.toSszList(SSZ_SCHEMA.getTransactionsSchema(), transactions));
  }

  public Bytes32 getParent_hash() {
    return getField0().get();
  }

  public Bytes32 getBlock_hash() {
    return getField1().get();
  }

  public Bytes getCoinbase() {
    return getField2().sszSerialize();
  }

  public Bytes32 getState_root() {
    return getField3().get();
  }

  public UInt64 getGas_limit() {
    return getField4().get();
  }

  public UInt64 getGas_used() {
    return getField5().get();
  }

  public Bytes32 getReceipt_root() {
    return getField6().get();
  }

  public Bytes getLogs_bloom() {
    return getField7().sszSerialize();
  }

  public UInt64 getDifficulty() {
    return getField8().get();
  }

  public SszList<Eth1Transaction> getTransactions() {
    return getField9();
  }

  public static class ExecutableDataSchema
      extends ContainerSchema10<
          ExecutableData,
          SszPrimitives.SszBytes32,
          SszPrimitives.SszBytes32,
          SszVector<SszPrimitives.SszByte>,
          SszPrimitives.SszBytes32,
          SszPrimitives.SszUInt64,
          SszPrimitives.SszUInt64,
          SszPrimitives.SszBytes32,
          SszVector<SszPrimitives.SszByte>,
          SszPrimitives.SszUInt64,
          SszList<Eth1Transaction>> {

    public ExecutableDataSchema() {
      super(
          "ExecutableData",
          namedSchema("parent_hash", SszPrimitiveSchemas.BYTES32_SCHEMA),
          namedSchema("block_hash", SszPrimitiveSchemas.BYTES32_SCHEMA),
          namedSchema("coinbase", SszVectorSchema.create(SszPrimitiveSchemas.BYTE_SCHEMA, 20)),
          namedSchema("state_root", SszPrimitiveSchemas.BYTES32_SCHEMA),
          namedSchema("gas_limit", SszPrimitiveSchemas.UINT64_SCHEMA),
          namedSchema("gas_used", SszPrimitiveSchemas.UINT64_SCHEMA),
          namedSchema("receipt_root", SszPrimitiveSchemas.BYTES32_SCHEMA),
          namedSchema(
              "logs_bloom",
              SszVectorSchema.create(SszPrimitiveSchemas.BYTE_SCHEMA, BYTES_PER_LOGS_BLOOM)),
          namedSchema("difficulty", SszPrimitiveSchemas.UINT64_SCHEMA),
          namedSchema(
              "transactions",
              SszListSchema.create(Eth1Transaction.SSZ_SCHEMA, MAX_ETH1_TRANSACTIONS)));
    }

    public SszSchema<SszList<Eth1Transaction>> getTransactionsSchema() {
      return getFieldSchema9();
    }

    @Override
    public ExecutableData createFromBackingNode(TreeNode node) {
      return new ExecutableData(this, node);
    }
  }
}
