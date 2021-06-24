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

package tech.pegasys.teku.spec.executionengine.client.schema;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.executionengine.client.serializer.BytesSerializer;
import tech.pegasys.teku.spec.executionengine.client.serializer.UInt64AsHexSerializer;

public class AssembleBlockRequest {
  @JsonSerialize(using = BytesSerializer.class)
  public final Bytes32 parentHash;

  @JsonSerialize(using = UInt64AsHexSerializer.class)
  public final UInt64 timestamp;

  @JsonSerialize(using = UInt64AsHexSerializer.class)
  public final UInt64 slot;

  @JsonSerialize(contentUsing = BytesSerializer.class)
  public final List<Bytes32> recentBeaconBlockRoots;

  public AssembleBlockRequest(
      Bytes32 parentHash, UInt64 timestamp, UInt64 slot, List<Bytes32> recentBeaconBlockRoots) {
    this.parentHash = parentHash;
    this.timestamp = timestamp;
    this.slot = slot;
    this.recentBeaconBlockRoots = recentBeaconBlockRoots;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AssembleBlockRequest that = (AssembleBlockRequest) o;
    return parentHash.equals(that.parentHash)
        && timestamp.equals(that.timestamp)
        && slot.equals(that.slot)
        && recentBeaconBlockRoots.equals(that.recentBeaconBlockRoots);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentHash, timestamp, slot, recentBeaconBlockRoots);
  }

  @Override
  public String toString() {
    return "AssembleBlockRequest{"
        + "parentHash="
        + parentHash
        + ", timestamp="
        + timestamp
        + ", slot="
        + slot
        + ", recentBeaconBlockRoots="
        + recentBeaconBlockRoots
        + '}';
  }
}
