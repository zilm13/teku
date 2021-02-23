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

import static tech.pegasys.teku.util.config.Constants.MAX_ETH1_TRANSACTIONS;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.provider.Base64BytesDeserializer;
import tech.pegasys.teku.provider.Base64BytesSerializer;
import tech.pegasys.teku.provider.Bytes20Deserializer;
import tech.pegasys.teku.provider.Bytes20Serializer;
import tech.pegasys.teku.provider.Bytes32Deserializer;
import tech.pegasys.teku.provider.Bytes32Serializer;
import tech.pegasys.teku.provider.UInt64AsNumberSerializer;
import tech.pegasys.teku.provider.UInt64Deserializer;
import tech.pegasys.teku.ssz.SSZTypes.Bytes20;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;

public class ExecutableData {

  @JsonSerialize(using = Bytes32Serializer.class)
  @JsonDeserialize(using = Bytes32Deserializer.class)
  public final Bytes32 parent_hash;

  @JsonSerialize(using = Bytes32Serializer.class)
  @JsonDeserialize(using = Bytes32Deserializer.class)
  public final Bytes32 block_hash;

  @JsonSerialize(using = Bytes20Serializer.class)
  @JsonDeserialize(using = Bytes20Deserializer.class)
  public final Bytes20 coinbase;

  @JsonSerialize(using = Bytes32Serializer.class)
  @JsonDeserialize(using = Bytes32Deserializer.class)
  public final Bytes32 state_root;

  @JsonSerialize(using = UInt64AsNumberSerializer.class)
  @JsonDeserialize(using = UInt64Deserializer.class)
  public final UInt64 gas_limit;

  @JsonSerialize(using = UInt64AsNumberSerializer.class)
  @JsonDeserialize(using = UInt64Deserializer.class)
  public final UInt64 gas_used;

  @JsonSerialize(using = Bytes32Serializer.class)
  @JsonDeserialize(using = Bytes32Deserializer.class)
  public final Bytes32 receipt_root;

  @JsonSerialize(using = Base64BytesSerializer.class)
  @JsonDeserialize(using = Base64BytesDeserializer.class)
  public final Bytes logs_bloom;

  @JsonSerialize(using = UInt64AsNumberSerializer.class)
  @JsonDeserialize(using = UInt64Deserializer.class)
  public final UInt64 difficulty;

  public final List<Eth1Transaction> transactions;

  public ExecutableData(
      @JsonProperty("parent_hash") Bytes32 parent_hash,
      @JsonProperty("block_hash") Bytes32 block_hash,
      @JsonProperty("coinbase") Bytes20 coinbase,
      @JsonProperty("state_root") Bytes32 state_root,
      @JsonProperty("gas_limit") UInt64 gas_limit,
      @JsonProperty("gas_used") UInt64 gas_used,
      @JsonProperty("receipt_root") Bytes32 receipt_root,
      @JsonProperty("logs_bloom") Bytes logs_bloom,
      @JsonProperty("difficulty") UInt64 difficulty,
      @JsonProperty("transactions") List<Eth1Transaction> transactions) {
    this.parent_hash = parent_hash;
    this.block_hash = block_hash;
    this.coinbase = coinbase;
    this.state_root = state_root;
    this.gas_limit = gas_limit;
    this.gas_used = gas_used;
    this.receipt_root = receipt_root;
    this.logs_bloom = logs_bloom;
    this.difficulty = difficulty;
    this.transactions = transactions != null ? transactions : Collections.emptyList();
  }

  public ExecutableData(
      tech.pegasys.teku.datastructures.blocks.exec.ExecutableData executableData) {
    this.parent_hash = executableData.getParent_hash();
    this.block_hash = executableData.getBlock_hash();
    this.coinbase = new Bytes20(executableData.getCoinbase());
    this.state_root = executableData.getState_root();
    this.gas_limit = executableData.getGas_limit();
    this.gas_used = executableData.getGas_used();
    this.receipt_root = executableData.getReceipt_root();
    this.logs_bloom = executableData.getLogs_bloom();
    this.difficulty = executableData.getDifficulty();
    this.transactions =
        executableData.getTransactions().stream()
            .map(Eth1Transaction::new)
            .collect(Collectors.toList());
  }

  public tech.pegasys.teku.datastructures.blocks.exec.ExecutableData asInternalExecutableData() {
    return new tech.pegasys.teku.datastructures.blocks.exec.ExecutableData(
        parent_hash,
        block_hash,
        coinbase.getWrappedBytes(),
        state_root,
        gas_limit,
        gas_used,
        receipt_root,
        logs_bloom,
        difficulty,
        SSZList.createMutable(
            transactions.stream().map(Eth1Transaction::asInternalEth1Transaction),
            MAX_ETH1_TRANSACTIONS,
            tech.pegasys.teku.datastructures.blocks.exec.Eth1Transaction.class));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExecutableData that = (ExecutableData) o;
    return Objects.equals(parent_hash, that.parent_hash)
        && Objects.equals(block_hash, that.block_hash)
        && Objects.equals(coinbase, that.coinbase)
        && Objects.equals(state_root, that.state_root)
        && Objects.equals(gas_limit, that.gas_limit)
        && Objects.equals(gas_used, that.gas_used)
        && Objects.equals(receipt_root, that.receipt_root)
        && Objects.equals(logs_bloom, that.logs_bloom)
        && Objects.equals(difficulty, that.difficulty)
        && Objects.equals(transactions, that.transactions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        parent_hash,
        block_hash,
        coinbase,
        state_root,
        gas_limit,
        gas_used,
        receipt_root,
        logs_bloom,
        difficulty,
        transactions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("parent_hash", parent_hash.slice(0, 8))
        .add("block_hash", block_hash.slice(0, 8))
        .add("coinbase", coinbase.getWrappedBytes().slice(0, 8))
        .add("state_root", state_root.slice(0, 8))
        .add("gas_limit", gas_limit)
        .add("gas_used", gas_used)
        .add("receipt_root", receipt_root.slice(0, 8))
        .add("logs_bloom", logs_bloom.slice(0, 8))
        .add("difficulty", difficulty)
        .add("transactions", transactions)
        .toString();
  }
}
