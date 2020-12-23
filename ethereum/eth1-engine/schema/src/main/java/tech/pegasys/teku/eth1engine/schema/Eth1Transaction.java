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

package tech.pegasys.teku.eth1engine.schema;

import com.google.common.base.MoreObjects;

public class Eth1Transaction {
  private String nonce;
  private String gasPrice;
  private String gas;
  private String to;
  private String value;
  private String input;
  private String v;
  private String r;
  private String s;

  public Eth1Transaction() {}

  public Eth1Transaction(
      String nonce,
      String gasPrice,
      String gas_limit,
      String recipient,
      String value,
      String input,
      String v,
      String r,
      String s) {
    this.nonce = nonce;
    this.gasPrice = gasPrice;
    this.gas = gas_limit;
    this.to = recipient;
    this.value = value;
    this.input = input;
    this.v = v;
    this.r = r;
    this.s = s;
  }

  public String getNonce() {
    return nonce;
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public String getGasPrice() {
    return gasPrice;
  }

  public void setGasPrice(String gasPrice) {
    this.gasPrice = gasPrice;
  }

  public String getGas() {
    return gas;
  }

  public void setGas(String gas) {
    this.gas = gas;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public String getV() {
    return v;
  }

  public void setV(String v) {
    this.v = v;
  }

  public String getR() {
    return r;
  }

  public void setR(String r) {
    this.r = r;
  }

  public String getS() {
    return s;
  }

  public void setS(String s) {
    this.s = s;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("nonce", nonce)
        .add("gasPrice", gasPrice)
        .add("gas", gas)
        .add("to", to)
        .add("value", value)
        .add("input", input)
        .add("v", v)
        .add("r", r)
        .add("s", s)
        .toString();
  }
}
