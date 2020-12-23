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

package tech.pegasys.teku.exec.eth1engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.provider.Bytes32Serializer;
import tech.pegasys.teku.provider.UInt64AsNumberSerializer;

public class ProduceBlockRequest {
  @JsonProperty("parent_hash")
  @JsonSerialize(using = Bytes32Serializer.class)
  public final Bytes32 parentHash;

  @JsonProperty("randao_mix")
  @JsonSerialize(using = Bytes32Serializer.class)
  public final Bytes32 randaoMix;

  @JsonProperty("slot")
  @JsonSerialize(using = UInt64AsNumberSerializer.class)
  public final UInt64 slot;

  @JsonProperty("timestamp")
  @JsonSerialize(using = UInt64AsNumberSerializer.class)
  public final UInt64 timestamp;

  @JsonProperty("recent_beacon_block_roots")
  @JsonSerialize(contentUsing = Bytes32Serializer.class)
  public final List<Bytes32> recentBeaconBlockRoots;

  public ProduceBlockRequest(
      Bytes32 parentHash,
      Bytes32 randaoMix,
      UInt64 slot,
      UInt64 timestamp,
      List<Bytes32> recentBeaconBlockRoots) {
    this.parentHash = parentHash;
    this.randaoMix = randaoMix;
    this.slot = slot;
    this.timestamp = timestamp;
    this.recentBeaconBlockRoots = recentBeaconBlockRoots;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProduceBlockRequest that = (ProduceBlockRequest) o;
    return Objects.equals(parentHash, that.parentHash)
        && Objects.equals(randaoMix, that.randaoMix)
        && Objects.equals(slot, that.slot)
        && Objects.equals(timestamp, that.timestamp)
        && Objects.equals(recentBeaconBlockRoots, that.recentBeaconBlockRoots);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentHash, randaoMix, slot, timestamp, recentBeaconBlockRoots);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("parentHash", parentHash)
        .add("randaoMix", randaoMix)
        .add("slot", slot)
        .add("timestamp", timestamp)
        .add("recentBeaconBlockRoots", recentBeaconBlockRoots)
        .toString();
  }
}
