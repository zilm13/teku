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

package tech.pegasys.teku.exec.eth1engine.schema;

import java.math.BigInteger;
import java.util.List;

public class ExecutableDTO {
  private String block_hash;
  private String coinbase;
  private String state_root;
  private Long gas_limit;
  private Long gas_used;
  private String receipt_root;
  private String logs_bloom;
  private Long number;
  private BigInteger difficulty;
  private List<Eth1TransactionDTO> transactions;

  public ExecutableDTO(
      String block_hash,
      String coinbase,
      String state_root,
      Long gas_limit,
      Long gas_used,
      String receipt_root,
      String logs_bloom,
      Long number,
      BigInteger difficulty,
      List<Eth1TransactionDTO> transactions) {
    this.block_hash = block_hash;
    this.coinbase = coinbase;
    this.state_root = state_root;
    this.gas_limit = gas_limit;
    this.gas_used = gas_used;
    this.receipt_root = receipt_root;
    this.logs_bloom = logs_bloom;
    this.number = number;
    this.difficulty = difficulty;
    this.transactions = transactions;
  }

  public String getBlock_hash() {
    return block_hash;
  }

  public void setBlock_hash(String block_hash) {
    this.block_hash = block_hash;
  }

  public String getCoinbase() {
    return coinbase;
  }

  public void setCoinbase(String coinbase) {
    this.coinbase = coinbase;
  }

  public String getState_root() {
    return state_root;
  }

  public void setState_root(String state_root) {
    this.state_root = state_root;
  }

  public Long getGas_limit() {
    return gas_limit;
  }

  public void setGas_limit(Long gas_limit) {
    this.gas_limit = gas_limit;
  }

  public Long getGas_used() {
    return gas_used;
  }

  public void setGas_used(Long gas_used) {
    this.gas_used = gas_used;
  }

  public String getReceipt_root() {
    return receipt_root;
  }

  public void setReceipt_root(String receipt_root) {
    this.receipt_root = receipt_root;
  }

  public String getLogs_bloom() {
    return logs_bloom;
  }

  public void setLogs_bloom(String logs_bloom) {
    this.logs_bloom = logs_bloom;
  }

  public Long getNumber() {
    return number;
  }

  public void setNumber(Long number) {
    this.number = number;
  }

  public BigInteger getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(BigInteger difficulty) {
    this.difficulty = difficulty;
  }

  public List<Eth1TransactionDTO> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<Eth1TransactionDTO> transactions) {
    this.transactions = transactions;
  }
}
