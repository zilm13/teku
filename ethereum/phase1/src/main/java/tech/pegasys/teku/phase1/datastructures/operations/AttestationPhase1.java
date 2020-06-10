/*
 * Copyright 2019 ConsenSys AG.
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

package tech.pegasys.teku.phase1.datastructures.operations;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.google.common.primitives.UnsignedLong;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.datastructures.util.SimpleOffsetSerializer;
import tech.pegasys.teku.ssz.SSZTypes.Bitlist;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.HashTreeUtil.SSZTypes;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class AttestationPhase1 implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  public static final int SSZ_FIELD_COUNT = 2;

  private Bitlist aggregation_bits; // Bitlist bounded by MAX_VALIDATORS_PER_COMMITTEE
  private AttestationDataPhase1 data;
  private SSZList<Bitlist> custody_bits_blocks; // List bounded by MAX_SHARD_BLOCKS_PER_ATTESTATION
  private BLSSignature signature;

  public AttestationPhase1(
      Bitlist aggregation_bits,
      AttestationDataPhase1 data,
      SSZList<Bitlist> custody_bits_blocks,
      BLSSignature signature) {
    this.aggregation_bits = aggregation_bits;
    this.data = data;
    this.custody_bits_blocks = custody_bits_blocks;
    this.signature = signature;
  }

  public AttestationPhase1(AttestationPhase1 attestation) {
    this.aggregation_bits = attestation.getAggregation_bits().copy();
    this.data = attestation.getData();
    this.custody_bits_blocks = attestation.getCustody_bits_blocks();
    this.signature = attestation.getAggregate_signature();
  }

  public UnsignedLong getEarliestSlotForForkChoiceProcessing() {
    return data.getEarliestSlotForForkChoice();
  }

  public Collection<Bytes32> getDependentBlockRoots() {
    return Sets.newHashSet(data.getTarget().getRoot(), data.getBeacon_block_root());
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT + data.getSSZFieldCount() + signature.getSSZFieldCount();
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList = new ArrayList<>();
    fixedPartsList.addAll(List.of(Bytes.EMPTY));
    fixedPartsList.addAll(data.get_fixed_parts());
    fixedPartsList.addAll(List.of(Bytes.EMPTY));
    fixedPartsList.addAll(signature.get_fixed_parts());
    return fixedPartsList;
  }

  @Override
  public List<Bytes> get_variable_parts() {
    List<Bytes> variablePartsList = new ArrayList<>();
    // TODO The below lines are a hack while Tuweni SSZ/SOS is being upgraded. To be uncommented
    // once we shift from Bitlist to a real bitlist type.
    // Bitlist serialized_aggregation_bits =
    // Bitlist.fromHexString("0x01").shiftLeft(aggregation_bits.bitLength()).or(aggregation_bits);
    // variablePartsList.addAll(List.of(serialized_aggregation_bits));
    variablePartsList.addAll(List.of(aggregation_bits.serialize()));
    variablePartsList.addAll(List.of(Bytes.EMPTY));
    variablePartsList.addAll(
        custody_bits_blocks.asList().stream().map(Bitlist::serialize).collect(Collectors.toList()));
    // TODO The below lines are a hack while Tuweni SSZ/SOS is being upgraded. To be uncommented
    // once we shift from Bitlist to a real bitlist type.
    // Bitlist serialized_custody_bitfield =
    // Bitlist.fromHexString("0x01").shiftLeft(aggregation_bits.bitLength()).or(custody_bitfield);
    // variablePartsList.addAll(List.of(serialized_custody_bitfield));
    variablePartsList.addAll(Collections.nCopies(signature.getSSZFieldCount(), Bytes.EMPTY));
    return variablePartsList;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttestationPhase1)) {
      return false;
    }
    AttestationPhase1 that = (AttestationPhase1) o;
    return Objects.equals(aggregation_bits, that.aggregation_bits)
        && Objects.equals(data, that.data)
        && Objects.equals(custody_bits_blocks, that.custody_bits_blocks)
        && Objects.equals(signature, that.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(aggregation_bits, data, custody_bits_blocks, signature);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("aggregation_bits", aggregation_bits)
        .add("data", data)
        .add("custody_bits_blocks", custody_bits_blocks)
        .add("signature", signature)
        .toString();
  }

  /** ******************* * GETTERS & SETTERS * * ******************* */
  public Bitlist getAggregation_bits() {
    return aggregation_bits;
  }

  public void setAggregation_bits(Bitlist aggregation_bits) {
    this.aggregation_bits = aggregation_bits;
  }

  public AttestationDataPhase1 getData() {
    return data;
  }

  public void setData(AttestationDataPhase1 data) {
    this.data = data;
  }

  public BLSSignature getAggregate_signature() {
    return signature;
  }

  public void setAggregate_signature(BLSSignature aggregate_signature) {
    this.signature = aggregate_signature;
  }

  public SSZList<Bitlist> getCustody_bits_blocks() {
    return custody_bits_blocks;
  }

  public void setCustody_bits_blocks(SSZList<Bitlist> custody_bits_blocks) {
    this.custody_bits_blocks = custody_bits_blocks;
  }

  public BLSSignature getSignature() {
    return signature;
  }

  public void setSignature(BLSSignature signature) {
    this.signature = signature;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root_bitlist(aggregation_bits),
            data.hash_tree_root(),
            HashTreeUtil.hash_tree_root_list_bytes(
                custody_bits_blocks.map(Bytes32.class, HashTreeUtil::hash_tree_root_bitlist)),
            HashTreeUtil.hash_tree_root(
                SSZTypes.VECTOR_OF_BASIC, SimpleOffsetSerializer.serialize(signature))));
  }
}
