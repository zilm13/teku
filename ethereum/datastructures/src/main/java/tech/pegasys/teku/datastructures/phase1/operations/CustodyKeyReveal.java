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

package tech.pegasys.teku.datastructures.phase1.operations;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.HashTreeUtil.SSZTypes;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class CustodyKeyReveal implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 1;

  private final UnsignedLong revealer_index;
  private final BLSSignature reveal;

  public CustodyKeyReveal(UnsignedLong revealer_index, BLSSignature reveal) {
    this.revealer_index = revealer_index;
    this.reveal = reveal;
  }

  @Override
  public int getSSZFieldCount() {
    return reveal.getSSZFieldCount() + SSZ_FIELD_COUNT;
  }

  public UnsignedLong getRevealer_index() {
    return revealer_index;
  }

  public BLSSignature getReveal() {
    return reveal;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root(
                SSZTypes.BASIC, SSZ.encodeUInt64(revealer_index.longValue())),
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, reveal.toBytes())));
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList = new ArrayList<>();
    fixedPartsList.add(SSZ.encodeUInt64(revealer_index.longValue()));
    fixedPartsList.addAll(reveal.get_fixed_parts());
    return fixedPartsList;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustodyKeyReveal)) {
      return false;
    }
    CustodyKeyReveal that = (CustodyKeyReveal) o;
    return Objects.equals(revealer_index, that.revealer_index)
        && Objects.equals(reveal, that.reveal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revealer_index, reveal);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("revealer_index", revealer_index)
        .add("reveal", reveal)
        .toString();
  }
}
