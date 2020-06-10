package tech.pegasys.teku.phase1.datastructures.shard;

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
import tech.pegasys.teku.datastructures.util.SimpleOffsetSerializer;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.HashTreeUtil.SSZTypes;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class ShardTransition implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 4;

  private final UnsignedLong start_slot;
  private final SSZList<UnsignedLong> shard_block_lengths;
  private final SSZList<Bytes32> shard_data_roots;
  private final SSZList<ShardState> shard_states;
  private final BLSSignature proposer_signature_aggregate;

  public ShardTransition(
      UnsignedLong start_slot,
      SSZList<UnsignedLong> shard_block_lengths,
      SSZList<Bytes32> shard_data_roots,
      SSZList<ShardState> shard_states,
      BLSSignature proposer_signature_aggregate) {
    this.start_slot = start_slot;
    this.shard_block_lengths = shard_block_lengths;
    this.shard_data_roots = shard_data_roots;
    this.shard_states = shard_states;
    this.proposer_signature_aggregate = proposer_signature_aggregate;
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT + proposer_signature_aggregate.getSSZFieldCount();
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    final List<Bytes> parts = new ArrayList<>();
    parts.addAll(List.of(SSZ.encodeInt64(start_slot.longValue())));
    parts.addAll(Collections.nCopies(3, Bytes.EMPTY));
    parts.addAll(proposer_signature_aggregate.get_fixed_parts());
    return parts;
  }

  @Override
  public List<Bytes> get_variable_parts() {
    return List.of(
        Bytes.EMPTY,
        Bytes.fromHexString(
            shard_block_lengths.stream()
                .map(value -> SSZ.encodeUInt64(value.longValue()).toHexString().substring(2))
                .collect(Collectors.joining())),
        Bytes.fromHexString(
            shard_data_roots.stream()
                .map(
                    value ->
                        SSZ.encode(writer -> writer.writeFixedBytes(value))
                            .toHexString()
                            .substring(2))
                .collect(Collectors.joining())),
        SimpleOffsetSerializer.serializeFixedCompositeList(shard_states),
        Bytes.EMPTY);
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root(SSZTypes.BASIC, SSZ.encodeUInt64(start_slot.longValue())),
            HashTreeUtil.hash_tree_root_list_ul(
                shard_block_lengths.map(Bytes.class, item -> SSZ.encodeUInt64(item.longValue()))),
            HashTreeUtil.hash_tree_root_list_bytes(shard_data_roots),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, shard_states),
            HashTreeUtil.hash_tree_root(
                SSZTypes.VECTOR_OF_BASIC, proposer_signature_aggregate.toBytes())));
  }

  public UnsignedLong getStart_slot() {
    return start_slot;
  }

  public SSZList<UnsignedLong> getShard_block_lengths() {
    return shard_block_lengths;
  }

  public SSZList<Bytes32> getShard_data_roots() {
    return shard_data_roots;
  }

  public SSZList<ShardState> getShard_states() {
    return shard_states;
  }

  public BLSSignature getProposer_signature_aggregate() {
    return proposer_signature_aggregate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShardTransition)) {
      return false;
    }
    ShardTransition that = (ShardTransition) o;
    return Objects.equals(start_slot, that.start_slot)
        && Objects.equals(shard_block_lengths, that.shard_block_lengths)
        && Objects.equals(shard_data_roots, that.shard_data_roots)
        && Objects.equals(shard_states, that.shard_states)
        && Objects.equals(proposer_signature_aggregate, that.proposer_signature_aggregate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        start_slot,
        shard_block_lengths,
        shard_data_roots,
        shard_states,
        proposer_signature_aggregate);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("slot", start_slot)
        .add("shard_block_lengths", shard_block_lengths)
        .add("shard_data_roots", shard_data_roots)
        .add("shard_states", shard_states)
        .add("proposer_signature_aggregate", proposer_signature_aggregate)
        .toString();
  }
}
