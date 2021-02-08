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

package tech.pegasys.teku.datastructures.state;

import com.google.common.base.MoreObjects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.Bytes48;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.datastructures.util.Merkleizable;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes4;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.backing.ContainerViewRead;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.type.VectorViewType;
import tech.pegasys.teku.ssz.backing.view.AbstractImmutableContainer;
import tech.pegasys.teku.ssz.backing.view.BasicViews;
import tech.pegasys.teku.ssz.backing.view.BasicViews.ByteView;
import tech.pegasys.teku.ssz.backing.view.BasicViews.Bytes32View;
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt64View;
import tech.pegasys.teku.ssz.backing.view.ViewUtils;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.ssz.sos.SszTypeDescriptor;

import java.util.ArrayList;
import java.util.List;

public class Withdrawal extends AbstractImmutableContainer
    implements ContainerViewRead, SimpleOffsetSerializable, Merkleizable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  public static final int SSZ_FIELD_COUNT = 5;

  @SszTypeDescriptor
  public static final ContainerViewType<Withdrawal> TYPE =
      ContainerViewType.create(
          List.of(
              new VectorViewType<ByteView>(BasicViewTypes.BYTE_TYPE, 48),
              BasicViewTypes.BYTES4_TYPE,
              BasicViewTypes.BYTES32_TYPE,
              BasicViewTypes.UINT64_TYPE,
              BasicViewTypes.UINT64_TYPE),
          Withdrawal::new);

  // BLS public key
  @SuppressWarnings("unused")
  private final Bytes48 pubkey = null;

  @SuppressWarnings("unused")
  private final Bytes4 withdrawal_target = null;

  // Withdrawal credentials
  @SuppressWarnings("unused")
  private final Bytes32 withdrawal_credentials = null;

  // Effective balance
  @SuppressWarnings("unused")
  private final UInt64 amount = null;

  // Epoch when validator withdrew
  @SuppressWarnings("unused")
  private final UInt64 withdrawal_epoch = null;

  private Withdrawal(ContainerViewType<Withdrawal> type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public Withdrawal(
      Bytes48 pubkey,
      Bytes4 withdrawal_target,
      Bytes32 withdrawal_credentials,
      UInt64 amount,
      UInt64 withdrawal_epoch) {
    super(
        TYPE,
        ViewUtils.createVectorFromBytes(pubkey),
        new BasicViews.Bytes4View(withdrawal_target),
        new Bytes32View(withdrawal_credentials),
        new UInt64View(amount),
        new UInt64View(withdrawal_epoch));
  }

  public Withdrawal(Withdrawal validator) {
    super(TYPE, validator.getBackingNode());
  }

  public Withdrawal() {
    super(TYPE);
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList = new ArrayList<>(List.of(
            getPubkey(),
            SSZ.encode(writer -> writer.writeFixedBytes(getWithdrawal_target().getWrappedBytes())),
            SSZ.encode(writer -> writer.writeFixedBytes(getWithdrawal_credentials())),
            SSZ.encodeUInt64(getAmount().longValue()),
            SSZ.encodeUInt64(getWithdrawal_epoch().longValue())));
    return fixedPartsList;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("pubkey", getPubkey())
        .add("withdrawal_target", getWithdrawal_target())
        .add("withdrawal_credentials", getWithdrawal_credentials())
        .add("amount", getAmount())
        .add("withdrawal_epoch", getWithdrawal_epoch())
        .toString();
  }

  public static Withdrawal create(
      Bytes48 pubkey,
      Bytes4 withdrawal_target,
      Bytes32 withdrawal_credentials,
      UInt64 amount,
      UInt64 withdrawal_epoch) {
    return new Withdrawal(
        pubkey,
        withdrawal_target,
        withdrawal_credentials,
        amount,
        withdrawal_epoch);
  }

  /**
   * Returns compressed BLS public key bytes
   *
   * <p>{@link BLSPublicKey} instance can be created with {@link
   * BLSPublicKey#fromBytesCompressed(Bytes48)} method. However this method is pretty 'expensive'
   * and the preferred way would be to use {@link
   * tech.pegasys.teku.datastructures.util.ValidatorsUtil#getValidatorPubKey(BeaconState, UInt64)}
   * if the {@link BeaconState} instance and validator index is available
   */
  public Bytes48 getPubkey() {
    return Bytes48.wrap(ViewUtils.getAllBytes(getAny(0)));
  }

  public Bytes4 getWithdrawal_target() {
    return ((BasicViews.Bytes4View) get(1)).get();
  }

  public Bytes32 getWithdrawal_credentials() {
    return ((Bytes32View) get(2)).get();
  }

  public UInt64 getAmount() {
    return ((UInt64View) get(3)).get();
  }

  public UInt64 getWithdrawal_epoch() {
    return ((UInt64View) get(4)).get();
  }

  public Withdrawal withAmount(UInt64 amount) {
    return create(
        getPubkey(),
        getWithdrawal_target(),
        getWithdrawal_credentials(),
        amount,
        getWithdrawal_epoch());
  }

  public Withdrawal withWithdrawable_epoch(UInt64 withdrawable_epoch) {
    return create(
        getPubkey(),
        getWithdrawal_target(),
        getWithdrawal_credentials(),
        getAmount(),
        withdrawable_epoch);
  }

  @Override
  public Bytes32 hash_tree_root() {
    return hashTreeRoot();
  }
}
