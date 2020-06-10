package tech.pegasys.teku.phase1.datastructures.shard;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;
import java.util.ArrayList;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.view.AbstractImmutableContainer;
import tech.pegasys.teku.ssz.backing.view.BasicViews.Bytes32View;
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt64View;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class ShardState extends AbstractImmutableContainer
    implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  public static final int SSZ_FIELD_COUNT = 4;

  public static final ContainerViewType<ShardState> TYPE =
      new ContainerViewType<>(
          List.of(
              BasicViewTypes.UINT64_TYPE,
              BasicViewTypes.UINT64_TYPE,
              BasicViewTypes.BYTES32_TYPE,
              BasicViewTypes.BYTES32_TYPE),
          ShardState::new);

  @SuppressWarnings("unused")
  private final UnsignedLong slot = null;

  @SuppressWarnings("unused")
  private final UnsignedLong gasprice = null;

  @SuppressWarnings("unused")
  private final Bytes32 transition_digest = null;

  @SuppressWarnings("unused")
  private final Bytes32 latest_block_root = null;

  public ShardState(ContainerViewType<ShardState> type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public ShardState(
      UnsignedLong slot,
      UnsignedLong gasprice,
      Bytes32 transition_digest,
      Bytes32 latest_block_root) {
    super(
        TYPE,
        new UInt64View(slot),
        new UInt64View(gasprice),
        new Bytes32View(transition_digest),
        new Bytes32View(latest_block_root));
  }

  public ShardState(ShardState state) {
    super(TYPE, state.getBackingNode());
  }

  public ShardState() {
    super(TYPE);
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList = new ArrayList<>();
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(getSlot().longValue())));
    fixedPartsList.addAll(List.of(SSZ.encodeUInt64(getGasprice().longValue())));
    fixedPartsList.addAll(
        List.of(SSZ.encode(writer -> writer.writeFixedBytes(getTransition_digest()))));
    fixedPartsList.addAll(
        List.of(SSZ.encode(writer -> writer.writeFixedBytes(getLatest_block_root()))));
    return fixedPartsList;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return hashTreeRoot();
  }

  public UnsignedLong getSlot() {
    return ((UInt64View) get(0)).get();
  }

  public UnsignedLong getGasprice() {
    return ((UInt64View) get(1)).get();
  }

  public Bytes32 getTransition_digest() {
    return ((Bytes32View) get(2)).get();
  }

  public Bytes32 getLatest_block_root() {
    return ((Bytes32View) get(3)).get();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("slot", getSlot())
        .add("gasprice", getGasprice())
        .add("transition_digest", getTransition_digest())
        .add("latest_block_root", getLatest_block_root())
        .toString();
  }
}
