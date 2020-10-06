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

package tech.pegasys.teku.datastructures.phase1.state;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.ssz.SSZTypes.SSZBackingList;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.backing.VectorViewRead;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.type.ListViewType;
import tech.pegasys.teku.ssz.backing.type.VectorViewType;
import tech.pegasys.teku.ssz.backing.view.AbstractBasicView;
import tech.pegasys.teku.ssz.backing.view.AbstractImmutableContainer;
import tech.pegasys.teku.ssz.backing.view.BasicViews.ByteView;
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt64View;
import tech.pegasys.teku.ssz.backing.view.ViewUtils;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.config.Constants;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class CompactCommittee extends AbstractImmutableContainer
    implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  private static final int SSZ_FIELD_COUNT = 2;

  public static final ContainerViewType<CompactCommittee> TYPE =
      new ContainerViewType<>(
          List.of(
              new ListViewType<VectorViewRead<ByteView>>(
                  new VectorViewType<ByteView>(BasicViewTypes.BYTE_TYPE, 48),
                  Constants.MAX_VALIDATORS_PER_COMMITTEE),
              new ListViewType<UInt64View>(
                  BasicViewTypes.UINT64_TYPE, Constants.MAX_VALIDATORS_PER_COMMITTEE)),
          CompactCommittee::new);

  @SuppressWarnings("unused")
  private final SSZList<BLSPublicKey> pubkeys = null;

  @SuppressWarnings("unused")
  private final SSZList<UnsignedLong> compact_validators = null;

  public CompactCommittee(ContainerViewType<CompactCommittee> type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public CompactCommittee(SSZList<BLSPublicKey> pubkeys, SSZList<UnsignedLong> compact_validators) {
    super(
        TYPE,
        ViewUtils.createListOfBytesView(pubkeys.map(Bytes.class, BLSPublicKey::toBytes), 48),
        ViewUtils.createListOfBasicsView(
            compact_validators, BasicViewTypes.UINT64_TYPE, UInt64View::new));
  }

  public CompactCommittee(CompactCommittee compactCommittee) {
    super(TYPE, compactCommittee.getBackingNode());
  }

  public CompactCommittee() {
    super(TYPE);
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    return Collections.nCopies(2, Bytes.EMPTY);
  }

  @Override
  public List<Bytes> get_variable_parts() {
    return List.of(
        Bytes.fromHexString(
            getPubkeys().stream()
                .map(value -> value.toBytes().toHexString().substring(2))
                .collect(Collectors.joining())),
        Bytes.fromHexString(
            getCompact_validators().stream()
                .map(value -> SSZ.encodeInt64(value.longValue()).toHexString().substring(2))
                .collect(Collectors.joining())));
  }

  @Override
  public Bytes32 hash_tree_root() {
    return hashTreeRoot();
  }

  public SSZList<BLSPublicKey> getPubkeys() {
    return new SSZBackingList<>(
        BLSPublicKey.class,
        getAny(0),
        pubKey -> ViewUtils.createVectorFromBytes(pubKey.toBytes()),
        bytes -> BLSPublicKey.fromBytes(ViewUtils.getAllBytes(bytes)));
  }

  public SSZList<UnsignedLong> getCompact_validators() {
    return new SSZBackingList<>(
        UnsignedLong.class, getAny(1), UInt64View::new, AbstractBasicView::get);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("pubkeys", getPubkeys())
        .add("compact_validators", getCompact_validators())
        .toString();
  }
}
