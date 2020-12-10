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

package tech.pegasys.teku.datastructures.blocks.exec;

import static tech.pegasys.teku.util.config.Constants.MAX_BYTES_PER_TRANSACTION_PAYLOAD;

import com.google.common.base.MoreObjects;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.datastructures.util.Merkleizable;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes20;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.backing.ListViewRead;
import tech.pegasys.teku.ssz.backing.ListViewWrite;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.type.ListViewType;
import tech.pegasys.teku.ssz.backing.type.VectorViewType;
import tech.pegasys.teku.ssz.backing.view.AbstractImmutableContainer;
import tech.pegasys.teku.ssz.backing.view.BasicViews.ByteView;
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt256View;
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt64View;
import tech.pegasys.teku.ssz.backing.view.ViewUtils;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;

public class Eth1Transaction extends AbstractImmutableContainer
    implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  public static final ContainerViewType<Eth1Transaction> TYPE =
      new ContainerViewType<>(
          List.of(
              BasicViewTypes.UINT64_TYPE,
              BasicViewTypes.UINT256_TYPE,
              BasicViewTypes.UINT64_TYPE,
              new VectorViewType<ByteView>(BasicViewTypes.BYTE_TYPE, Bytes20.SIZE),
              BasicViewTypes.UINT256_TYPE,
              new ListViewType<ByteView>(
                  BasicViewTypes.BYTE_TYPE, MAX_BYTES_PER_TRANSACTION_PAYLOAD),
              BasicViewTypes.UINT256_TYPE,
              BasicViewTypes.UINT256_TYPE,
              BasicViewTypes.UINT256_TYPE),
          Eth1Transaction::new);

  @SuppressWarnings("unused")
  private final UInt64 nonce = null;

  @SuppressWarnings("unused")
  private final UInt256 gas_price = null;

  @SuppressWarnings("unused")
  private final UInt64 gas_limit = null;

  @SuppressWarnings("unused")
  private final Bytes20 recipient = null;

  @SuppressWarnings("unused")
  private final UInt256 value = null;

  @SuppressWarnings("unused")
  private final SSZList<Byte> input =
      SSZList.createMutable(Byte.class, MAX_BYTES_PER_TRANSACTION_PAYLOAD);

  @SuppressWarnings("unused")
  private final UInt256 v = null;

  @SuppressWarnings("unused")
  private final UInt256 r = null;

  @SuppressWarnings("unused")
  private final UInt256 s = null;

  public Eth1Transaction() {
    super(TYPE);
  }

  public Eth1Transaction(
      ContainerViewType<? extends AbstractImmutableContainer> type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public Eth1Transaction(
      UInt64 nonce,
      UInt256 gas_price,
      UInt64 gas_limit,
      Bytes20 recipient,
      UInt256 value,
      Bytes input,
      UInt256 v,
      UInt256 r,
      UInt256 s) {
    super(
        TYPE,
        new UInt64View(nonce),
        new UInt256View(gas_price),
        new UInt64View(gas_limit),
        ViewUtils.createVectorFromBytes(recipient.getWrappedBytes()),
        new UInt256View(value),
        ViewUtils.createListFromBytes(input, MAX_BYTES_PER_TRANSACTION_PAYLOAD),
        new UInt256View(v),
        new UInt256View(r),
        new UInt256View(s));
  }

  public Eth1Transaction(
      UInt64 nonce,
      UInt256 gas_price,
      UInt64 gas_limit,
      Bytes20 recipient,
      UInt256 value,
      SSZList<Byte> input,
      UInt256 v,
      UInt256 r,
      UInt256 s) {
    super(
        TYPE,
        new UInt64View(nonce),
        new UInt256View(gas_price),
        new UInt64View(gas_limit),
        ViewUtils.createVectorFromBytes(recipient.getWrappedBytes()),
        new UInt256View(value),
        createInputView(input),
        new UInt256View(v),
        new UInt256View(r),
        new UInt256View(s));
  }

  private static ListViewRead<ByteView> createInputView(SSZList<Byte> list) {
    ListViewType<ByteView> type =
        new ListViewType<>(BasicViewTypes.BYTE_TYPE, MAX_BYTES_PER_TRANSACTION_PAYLOAD);
    ListViewWrite<ByteView> view = type.getDefault().createWritableCopy();
    for (int i = 0; i < list.size(); i++) {
      view.set(i, new ByteView(list.get(i)));
    }
    return view.commitChanges();
  }

  @Override
  public int getSSZFieldCount() {
    return 0;
  }

  public UInt64 getNonce() {
    return ((UInt64View) get(0)).get();
  }

  public UInt256 getGas_price() {
    return ((UInt256View) get(1)).get();
  }

  public UInt64 getGas_limit() {
    return ((UInt64View) get(2)).get();
  }

  public Bytes20 getRecipient() {
    return new Bytes20(ViewUtils.getAllBytes(getAny(3)));
  }

  public UInt256 getValue() {
    return ((UInt256View) get(4)).get();
  }

  public Bytes getInput() {
    return ViewUtils.getListBytes(getAny(5));
  }

  public UInt256 getV() {
    return ((UInt256View) get(6)).get();
  }

  public UInt256 getR() {
    return ((UInt256View) get(7)).get();
  }

  public UInt256 getS() {
    return ((UInt256View) get(8)).get();
  }

  @Override
  public Bytes32 hash_tree_root() {
    return hashTreeRoot();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("nonce", getNonce())
        .add("gas_price", getGas_price())
        .add("gas", getGas_limit())
        .add("to", getRecipient())
        .add("value", getValue())
        .add("input", getInput())
        .add("v", getV())
        .add("r", getR())
        .add("s", getS())
        .toString();
  }
}
