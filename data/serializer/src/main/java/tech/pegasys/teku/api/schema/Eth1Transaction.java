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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import java.io.IOException;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.provider.Bytes20Deserializer;
import tech.pegasys.teku.provider.Bytes20Serializer;
import tech.pegasys.teku.provider.BytesDeserializer;
import tech.pegasys.teku.provider.BytesSerializer;
import tech.pegasys.teku.provider.HexUInt64Deserializer;
import tech.pegasys.teku.provider.HexUInt64Serializer;
import tech.pegasys.teku.provider.UInt256Deserializer;
import tech.pegasys.teku.provider.UInt256Serializer;
import tech.pegasys.teku.ssz.SSZTypes.Bytes20;

public class Eth1Transaction {

  @JsonSerialize(using = HexUInt64Serializer.class)
  @JsonDeserialize(using = HexUInt64Deserializer.class)
  public final UInt64 nonce;

  @JsonProperty("gasPrice")
  @JsonSerialize(using = UInt256Serializer.class)
  @JsonDeserialize(using = UInt256Deserializer.class)
  public final UInt256 gas_price;

  @JsonProperty("gas")
  @JsonSerialize(using = HexUInt64Serializer.class)
  @JsonDeserialize(using = HexUInt64Deserializer.class)
  public final UInt64 gas_limit;

  @JsonProperty("to")
  @JsonSerialize(using = RecipientSerializer.class)
  @JsonDeserialize(using = Bytes20Deserializer.class)
  public final Bytes20 recipient;

  @JsonSerialize(using = UInt256Serializer.class)
  @JsonDeserialize(using = UInt256Deserializer.class)
  public final UInt256 value;

  @JsonSerialize(using = BytesSerializer.class)
  @JsonDeserialize(using = BytesDeserializer.class)
  public final Bytes input;

  @JsonSerialize(using = UInt256Serializer.class)
  @JsonDeserialize(using = UInt256Deserializer.class)
  public final UInt256 v;

  @JsonSerialize(using = UInt256Serializer.class)
  @JsonDeserialize(using = UInt256Deserializer.class)
  public final UInt256 r;

  @JsonSerialize(using = UInt256Serializer.class)
  @JsonDeserialize(using = UInt256Deserializer.class)
  public final UInt256 s;

  @JsonCreator
  public Eth1Transaction(
      @JsonProperty("nonce") final UInt64 nonce,
      @JsonProperty("gasPrice") final UInt256 gas_price,
      @JsonProperty("gas") final UInt64 gas_limit,
      @JsonProperty("to") final Bytes20 recipient,
      @JsonProperty("value") final UInt256 value,
      @JsonProperty("input") final Bytes input,
      @JsonProperty("v") final UInt256 v,
      @JsonProperty("r") final UInt256 r,
      @JsonProperty("s") final UInt256 s) {
    this.nonce = nonce;
    this.gas_price = gas_price;
    this.gas_limit = gas_limit;
    this.recipient = recipient != null ? recipient : Bytes20.ZERO;
    this.value = value;
    this.input = input;
    this.v = v;
    this.r = r;
    this.s = s;
  }

  public Eth1Transaction(
      tech.pegasys.teku.datastructures.blocks.exec.Eth1Transaction eth1Transaction) {
    this.nonce = eth1Transaction.getNonce();
    this.gas_price = eth1Transaction.getGas_price();
    this.gas_limit = eth1Transaction.getGas_limit();
    this.recipient = new Bytes20(eth1Transaction.getRecipient());
    this.value = eth1Transaction.getValue();
    this.input = eth1Transaction.getInput();
    this.v = eth1Transaction.getV();
    this.r = eth1Transaction.getR();
    this.s = eth1Transaction.getS();
  }

  public tech.pegasys.teku.datastructures.blocks.exec.Eth1Transaction asInternalEth1Transaction() {
    return new tech.pegasys.teku.datastructures.blocks.exec.Eth1Transaction(
        nonce, gas_price, gas_limit, recipient.getWrappedBytes(), value, input, v, r, s);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Eth1Transaction that = (Eth1Transaction) o;
    return Objects.equals(nonce, that.nonce)
        && Objects.equals(gas_price, that.gas_price)
        && Objects.equals(gas_limit, that.gas_limit)
        && Objects.equals(recipient, that.recipient)
        && Objects.equals(value, that.value)
        && Objects.equals(input, that.input)
        && Objects.equals(v, that.v)
        && Objects.equals(r, that.r)
        && Objects.equals(s, that.s);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nonce, gas_price, gas_limit, recipient, value, input, v, r, s);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("nonce", nonce)
        .add("gas_price", gas_price)
        .add("gas", gas_limit)
        .add("recipient", recipient)
        .add("value", value)
        .add("input", input)
        .add("v", v)
        .add("r", r)
        .add("s", s)
        .toString();
  }

  private static final class RecipientSerializer extends Bytes20Serializer {
    @Override
    public void serialize(Bytes20 value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (Bytes20.ZERO.equals(value)) {
        gen.writeNull();
      } else {
        super.serialize(value, gen, serializers);
      }
    }
  }
}
