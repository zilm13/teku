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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.HashTreeUtil.SSZTypes;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class AttestationCustodyBitWrapper
    implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 3;

  private final Bytes32 attestation_data_root;
  private final UnsignedLong block_index;
  private final Boolean bit;

  public AttestationCustodyBitWrapper(
      Bytes32 attestation_data_root, UnsignedLong block_index, Boolean bit) {
    this.attestation_data_root = attestation_data_root;
    this.block_index = block_index;
    this.bit = bit;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, attestation_data_root),
            HashTreeUtil.hash_tree_root(SSZTypes.BASIC, SSZ.encodeUInt64(block_index.longValue())),
            HashTreeUtil.hash_tree_root(SSZTypes.BASIC, SSZ.encodeBoolean(bit))));
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    return List.of(
        SSZ.encode(writer -> writer.writeFixedBytes(attestation_data_root)),
        SSZ.encodeUInt64(block_index.longValue()),
        SSZ.encodeBoolean(bit));
  }

  public Bytes32 getAttestation_data_root() {
    return attestation_data_root;
  }

  public UnsignedLong getBlock_index() {
    return block_index;
  }

  public Boolean getBit() {
    return bit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttestationCustodyBitWrapper)) {
      return false;
    }
    AttestationCustodyBitWrapper that = (AttestationCustodyBitWrapper) o;
    return Objects.equals(attestation_data_root, that.attestation_data_root)
        && Objects.equals(block_index, that.block_index)
        && Objects.equals(bit, that.bit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attestation_data_root, block_index, bit);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attestation_data_root", attestation_data_root)
        .add("block_index", block_index)
        .add("bit", bit)
        .toString();
  }
}
