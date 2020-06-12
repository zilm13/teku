package tech.pegasys.teku.datastructures.phase1.shard;

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

public class ShardBlockHeader implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 5;

  private final Bytes32 shard_parent_root;
  private final Bytes32 beacon_parent_root;
  private final UnsignedLong slot;
  private final UnsignedLong proposer_index;
  private final Bytes32 body_root;

  public ShardBlockHeader(
      Bytes32 shard_parent_root,
      Bytes32 beacon_parent_root,
      UnsignedLong slot,
      UnsignedLong proposer_index,
      Bytes32 body_root) {
    this.shard_parent_root = shard_parent_root;
    this.beacon_parent_root = beacon_parent_root;
    this.slot = slot;
    this.proposer_index = proposer_index;
    this.body_root = body_root;
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
        SSZ.encode(writer -> writer.writeFixedBytes(body_root)));
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
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, body_root)));
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

  public Bytes32 getBody_root() {
    return body_root;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShardBlockHeader)) {
      return false;
    }
    ShardBlockHeader that = (ShardBlockHeader) o;
    return Objects.equals(shard_parent_root, that.shard_parent_root)
        && Objects.equals(beacon_parent_root, that.beacon_parent_root)
        && Objects.equals(slot, that.slot)
        && Objects.equals(proposer_index, that.proposer_index)
        && Objects.equals(body_root, that.body_root);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shard_parent_root, beacon_parent_root, slot, proposer_index, body_root);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("shard_parent_root", shard_parent_root)
        .add("beacon_parent_root", beacon_parent_root)
        .add("slot", slot)
        .add("proposer_index", proposer_index)
        .add("body_root", body_root)
        .toString();
  }
}
