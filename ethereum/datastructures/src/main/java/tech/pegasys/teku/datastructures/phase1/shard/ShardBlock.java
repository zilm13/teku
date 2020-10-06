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

package tech.pegasys.teku.datastructures.phase1.shard;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.HashTreeUtil.SSZTypes;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class ShardBlock implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 5;

  private final Bytes32 shard_parent_root;
  private final Bytes32 beacon_parent_root;
  private final UnsignedLong slot;
  private final UnsignedLong proposer_index;
  private final SSZList<Byte> body;

  public ShardBlock(
      Bytes32 shard_parent_root,
      Bytes32 beacon_parent_root,
      UnsignedLong slot,
      UnsignedLong proposer_index,
      SSZList<Byte> body) {
    this.shard_parent_root = shard_parent_root;
    this.beacon_parent_root = beacon_parent_root;
    this.slot = slot;
    this.proposer_index = proposer_index;
    this.body = body;
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    return List.of(
        SSZ.encode(writer -> writer.writeFixedBytes(shard_parent_root)),
        SSZ.encode(writer -> writer.writeFixedBytes(beacon_parent_root)),
        SSZ.encodeUInt64(slot.longValue()),
        SSZ.encodeUInt64(proposer_index.longValue()),
        Bytes.EMPTY);
  }

  @Override
  public List<Bytes> get_variable_parts() {
    List<Bytes> variablePartsList = new ArrayList<>();
    variablePartsList.addAll(Collections.nCopies(4, Bytes.EMPTY));
    variablePartsList.addAll(
        List.of(
            Bytes.fromHexString(
                body.stream()
                    .map(value -> SSZ.encodeUInt8(value & 0xFF).toHexString().substring(2))
                    .collect(Collectors.joining()))));
    return variablePartsList;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, shard_parent_root),
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, beacon_parent_root),
            HashTreeUtil.hash_tree_root(SSZTypes.BASIC, SSZ.encodeUInt64(slot.longValue())),
            HashTreeUtil.hash_tree_root(
                SSZTypes.BASIC, SSZ.encodeUInt64(proposer_index.longValue())),
            HashTreeUtil.hash_tree_root_list_ul(
                body.map(Bytes.class, item -> SSZ.encodeUInt8(item & 0xFF)))));
  }

  public Bytes32 getShard_parent_root() {
    return shard_parent_root;
  }

  public Bytes32 getBeacon_parent_root() {
    return beacon_parent_root;
  }

  public UnsignedLong getSlot() {
    return slot;
  }

  public UnsignedLong getProposer_index() {
    return proposer_index;
  }

  public SSZList<Byte> getBody() {
    return body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShardBlock)) {
      return false;
    }
    ShardBlock that = (ShardBlock) o;
    return Objects.equals(shard_parent_root, that.shard_parent_root)
        && Objects.equals(beacon_parent_root, that.beacon_parent_root)
        && Objects.equals(slot, that.slot)
        && Objects.equals(proposer_index, that.proposer_index)
        && Objects.equals(body, that.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shard_parent_root, beacon_parent_root, slot, proposer_index, body);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("shard_parent_root", shard_parent_root)
        .add("beacon_parent_root", beacon_parent_root)
        .add("slot", slot)
        .add("proposer_index", proposer_index)
        .add("body", body)
        .toString();
  }
}
