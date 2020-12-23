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
import tech.pegasys.teku.api.schema.ExecutableData;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.provider.Bytes32Serializer;
import tech.pegasys.teku.provider.UInt64AsNumberSerializer;

public class InsertBlockRequest {
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

  @JsonProperty("executable_data")
  public final ExecutableData executableData;

  public InsertBlockRequest(
      Bytes32 randaoMix,
      UInt64 slot,
      UInt64 timestamp,
      List<Bytes32> recentBeaconBlockRoots,
      ExecutableData executableData) {
    this.randaoMix = randaoMix;
    this.slot = slot;
    this.timestamp = timestamp;
    this.recentBeaconBlockRoots = recentBeaconBlockRoots;
    this.executableData = executableData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InsertBlockRequest that = (InsertBlockRequest) o;
    return Objects.equals(randaoMix, that.randaoMix)
        && Objects.equals(slot, that.slot)
        && Objects.equals(timestamp, that.timestamp)
        && Objects.equals(recentBeaconBlockRoots, that.recentBeaconBlockRoots)
        && Objects.equals(executableData, that.executableData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(randaoMix, slot, timestamp, recentBeaconBlockRoots, executableData);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("randaoMix", randaoMix)
        .add("slot", slot)
        .add("timestamp", timestamp)
        .add("recentBeaconBlockRoots", recentBeaconBlockRoots)
        .add("executableData", executableData)
        .toString();
  }
}
