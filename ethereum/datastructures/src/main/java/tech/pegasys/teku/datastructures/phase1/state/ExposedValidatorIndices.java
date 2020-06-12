package tech.pegasys.teku.datastructures.phase1.state;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.datastructures.phase1.config.ConstantsPhase1;
import tech.pegasys.teku.ssz.SSZTypes.SSZBackingList;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.type.ListViewType;
import tech.pegasys.teku.ssz.backing.view.AbstractBasicView;
import tech.pegasys.teku.ssz.backing.view.AbstractImmutableContainer;
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt64View;
import tech.pegasys.teku.ssz.backing.view.ViewUtils;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.config.Constants;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class ExposedValidatorIndices extends AbstractImmutableContainer
    implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 1;

  public static final ContainerViewType<ExposedValidatorIndices> TYPE =
      new ContainerViewType<>(
          List.of(
              new ListViewType<UInt64View>(
                  BasicViewTypes.UINT64_TYPE,
                  ConstantsPhase1.MAX_EARLY_DERIVED_SECRET_REVEALS * Constants.SLOTS_PER_EPOCH)),
          ExposedValidatorIndices::new);

  @SuppressWarnings("unused")
  private final SSZList<UnsignedLong> indices = null;

  public ExposedValidatorIndices(
      ContainerViewType<ExposedValidatorIndices> type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public ExposedValidatorIndices(SSZList<UnsignedLong> indices) {
    super(
        TYPE,
        ViewUtils.createListOfBasicsView(indices, BasicViewTypes.UINT64_TYPE, UInt64View::new));
  }

  public ExposedValidatorIndices(ExposedValidatorIndices exposedValidatorIndices) {
    super(TYPE, exposedValidatorIndices.getBackingNode());
  }

  public ExposedValidatorIndices() {
    super(TYPE);
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    return List.of(Bytes.EMPTY);
  }

  @Override
  public List<Bytes> get_variable_parts() {
    return List.of(
        Bytes.fromHexString(
            getIndices().stream()
                .map(value -> SSZ.encodeUInt64(value.longValue()).toHexString().substring(2))
                .collect(Collectors.joining())));
  }

  @Override
  public Bytes32 hash_tree_root() {
    return hashTreeRoot();
  }

  public SSZList<UnsignedLong> getIndices() {
    return new SSZBackingList<>(
        UnsignedLong.class, getAny(0), UInt64View::new, AbstractBasicView::get);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExposedValidatorIndices)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ExposedValidatorIndices that = (ExposedValidatorIndices) o;
    return Objects.equals(indices, that.indices);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), indices);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("indices", indices).toString();
  }
}
