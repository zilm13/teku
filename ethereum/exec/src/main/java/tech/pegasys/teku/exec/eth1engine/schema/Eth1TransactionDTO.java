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

public class Eth1TransactionDTO {
  private Long nonce;
  private BigInteger gas_price;
  private Long gas_limit;
  private String recipient;
  private BigInteger value;
  private String input;
  private BigInteger v;
  private BigInteger r;
  private BigInteger s;

  public Eth1TransactionDTO(
      Long nonce,
      BigInteger gas_price,
      Long gas_limit,
      String recipient,
      BigInteger value,
      String input,
      BigInteger v,
      BigInteger r,
      BigInteger s) {
    this.nonce = nonce;
    this.gas_price = gas_price;
    this.gas_limit = gas_limit;
    this.recipient = recipient;
    this.value = value;
    this.input = input;
    this.v = v;
    this.r = r;
    this.s = s;
  }

  public Long getNonce() {
    return nonce;
  }

  public void setNonce(Long nonce) {
    this.nonce = nonce;
  }

  public BigInteger getGas_price() {
    return gas_price;
  }

  public void setGas_price(BigInteger gas_price) {
    this.gas_price = gas_price;
  }

  public Long getGas_limit() {
    return gas_limit;
  }

  public void setGas_limit(Long gas_limit) {
    this.gas_limit = gas_limit;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public BigInteger getValue() {
    return value;
  }

  public void setValue(BigInteger value) {
    this.value = value;
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public BigInteger getV() {
    return v;
  }

  public void setV(BigInteger v) {
    this.v = v;
  }

  public BigInteger getR() {
    return r;
  }

  public void setR(BigInteger r) {
    this.r = r;
  }

  public BigInteger getS() {
    return s;
  }

  public void setS(BigInteger s) {
    this.s = s;
  }
}
