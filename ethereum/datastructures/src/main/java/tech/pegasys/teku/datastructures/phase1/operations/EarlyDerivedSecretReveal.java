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

public class EarlyDerivedSecretReveal
    implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 4;

  private UnsignedLong revealed_index;
  private UnsignedLong epoch;
  private BLSSignature reveal;
  private UnsignedLong masker_index;
  private Bytes32 mask;

  public EarlyDerivedSecretReveal(
      UnsignedLong revealed_index,
      UnsignedLong epoch,
      BLSSignature reveal,
      UnsignedLong masker_index,
      Bytes32 mask) {
    this.revealed_index = revealed_index;
    this.epoch = epoch;
    this.reveal = reveal;
    this.masker_index = masker_index;
    this.mask = mask;
  }

  @Override
  public int getSSZFieldCount() {
    return reveal.getSSZFieldCount() + SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList = new ArrayList<>();
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(revealed_index.longValue())));
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(epoch.longValue())));
    fixedPartsList.addAll(reveal.get_fixed_parts());
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(masker_index.longValue())));
    fixedPartsList.addAll(List.of(SSZ.encode(writer -> writer.writeFixedBytes(mask))));
    return fixedPartsList;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root(
                SSZTypes.BASIC, SSZ.encodeUInt64(revealed_index.longValue())),
            HashTreeUtil.hash_tree_root(SSZTypes.BASIC, SSZ.encodeUInt64(epoch.longValue())),
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, reveal.toBytes()),
            HashTreeUtil.hash_tree_root(SSZTypes.BASIC, SSZ.encodeUInt64(masker_index.longValue())),
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, mask)));
  }

  public UnsignedLong getRevealed_index() {
    return revealed_index;
  }

  public UnsignedLong getEpoch() {
    return epoch;
  }

  public BLSSignature getReveal() {
    return reveal;
  }

  public UnsignedLong getMasker_index() {
    return masker_index;
  }

  public Bytes32 getMask() {
    return mask;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EarlyDerivedSecretReveal)) {
      return false;
    }
    EarlyDerivedSecretReveal that = (EarlyDerivedSecretReveal) o;
    return Objects.equals(revealed_index, that.revealed_index)
        && Objects.equals(epoch, that.epoch)
        && Objects.equals(reveal, that.reveal)
        && Objects.equals(masker_index, that.masker_index)
        && Objects.equals(mask, that.mask);
  }

  @Override
  public int hashCode() {
    return Objects.hash(revealed_index, epoch, reveal, masker_index, mask);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("revealed_index", revealed_index)
        .add("epoch", epoch)
        .add("reveal", reveal)
        .add("masker_index", masker_index)
        .add("mask", mask)
        .toString();
  }
}
