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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.datastructures.phase1.shard.ShardTransition;
import tech.pegasys.teku.datastructures.util.SimpleOffsetSerializer;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.HashTreeUtil.SSZTypes;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class CustodySlashing implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 4;

  private final UnsignedLong data_index;
  private final UnsignedLong malefactor_index;
  private final BLSSignature malefactor_secret;
  private final UnsignedLong whistleblower_index;
  private final ShardTransition shard_transition;
  private final AttestationPhase1 attestation;
  private final SSZList<Byte> data;

  public CustodySlashing(
      UnsignedLong data_index,
      UnsignedLong malefactor_index,
      BLSSignature malefactor_secret,
      UnsignedLong whistleblower_index,
      ShardTransition shard_transition,
      AttestationPhase1 attestation,
      SSZList<Byte> data) {
    this.data_index = data_index;
    this.malefactor_index = malefactor_index;
    this.malefactor_secret = malefactor_secret;
    this.whistleblower_index = whistleblower_index;
    this.shard_transition = shard_transition;
    this.attestation = attestation;
    this.data = data;
  }

  @Override
  public int getSSZFieldCount() {
    return malefactor_secret.getSSZFieldCount()
        + shard_transition.getSSZFieldCount()
        + attestation.getSSZFieldCount()
        + SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList = new ArrayList<>();
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(data_index.longValue())));
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(malefactor_index.longValue())));
    fixedPartsList.addAll(malefactor_secret.get_fixed_parts());
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(whistleblower_index.longValue())));
    fixedPartsList.addAll(shard_transition.get_fixed_parts());
    fixedPartsList.addAll(attestation.get_fixed_parts());
    fixedPartsList.addAll(List.of(Bytes.EMPTY));
    return fixedPartsList;
  }

  @Override
  public List<Bytes> get_variable_parts() {
    List<Bytes> variablePartsList = new ArrayList<>();
    variablePartsList.addAll(Collections.nCopies(2, Bytes.EMPTY));
    variablePartsList.addAll(List.of(SimpleOffsetSerializer.serialize(malefactor_secret)));
    variablePartsList.addAll(List.of(SimpleOffsetSerializer.serialize(shard_transition)));
    variablePartsList.addAll(List.of(SimpleOffsetSerializer.serialize(attestation)));
    variablePartsList.addAll(
        List.of(
            Bytes.fromHexString(
                data.stream()
                    .map(value -> SSZ.encodeUInt8(value & 0xFF).toHexString().substring(2))
                    .collect(Collectors.joining()))));
    return variablePartsList;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root(SSZTypes.BASIC, SSZ.encodeUInt64(data_index.longValue())),
            HashTreeUtil.hash_tree_root(
                SSZTypes.BASIC, SSZ.encodeUInt64(malefactor_index.longValue())),
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, malefactor_secret.toBytes()),
            HashTreeUtil.hash_tree_root(
                SSZTypes.BASIC, SSZ.encodeUInt64(whistleblower_index.longValue())),
            shard_transition.hash_tree_root(),
            attestation.hash_tree_root(),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_BASIC, data)));
  }

  public UnsignedLong getData_index() {
    return data_index;
  }

  public UnsignedLong getMalefactor_index() {
    return malefactor_index;
  }

  public BLSSignature getMalefactor_secret() {
    return malefactor_secret;
  }

  public UnsignedLong getWhistleblower_index() {
    return whistleblower_index;
  }

  public ShardTransition getShard_transition() {
    return shard_transition;
  }

  public AttestationPhase1 getAttestation() {
    return attestation;
  }

  public SSZList<Byte> getData() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustodySlashing)) {
      return false;
    }
    CustodySlashing that = (CustodySlashing) o;
    return Objects.equals(data_index, that.data_index)
        && Objects.equals(malefactor_index, that.malefactor_index)
        && Objects.equals(malefactor_secret, that.malefactor_secret)
        && Objects.equals(whistleblower_index, that.whistleblower_index)
        && Objects.equals(shard_transition, that.shard_transition)
        && Objects.equals(attestation, that.attestation)
        && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        data_index,
        malefactor_index,
        malefactor_secret,
        whistleblower_index,
        shard_transition,
        attestation,
        data);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("data_index", data_index)
        .add("malefactor_index", malefactor_index)
        .add("malefactor_secret", malefactor_secret)
        .add("whistleblower_index", whistleblower_index)
        .add("shard_transition", shard_transition)
        .add("attestation", attestation)
        .add("data", data)
        .toString();
  }
}
