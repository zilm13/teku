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

import com.google.common.primitives.UnsignedLong;
import jdk.jfr.Label;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.datastructures.blocks.Eth1Data;
import tech.pegasys.teku.datastructures.state.BeaconStateCache;
import tech.pegasys.teku.datastructures.state.MutableBeaconState;
import tech.pegasys.teku.datastructures.state.TransitionCaches;
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList;
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableVector;
import tech.pegasys.teku.ssz.backing.ViewRead;
import tech.pegasys.teku.ssz.backing.cache.IntCache;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.view.ContainerViewWriteImpl;

class MutableBeaconStatePhase1Impl extends ContainerViewWriteImpl
    implements MutableBeaconStatePhase1, BeaconStateCache {

  static MutableBeaconStatePhase1Impl createBuilder() {
    return new MutableBeaconStatePhase1Impl(new BeaconStatePhase1Impl(), true);
  }

  @Label("sos-ignore")
  private final TransitionCaches transitionCaches;

  @Label("sos-ignore")
  private final boolean builder;

  private SSZMutableList<ValidatorPhase1> validators;
  private SSZMutableList<UnsignedLong> balances;
  private SSZMutableVector<Bytes32> blockRoots;
  private SSZMutableVector<Bytes32> stateRoots;
  private SSZMutableList<Bytes32> historicalRoots;
  private SSZMutableList<Eth1Data> eth1DataVotes;
  private SSZMutableVector<Bytes32> randaoMixes;
  private SSZMutableList<PendingAttestationPhase1> previousEpochAttestations;
  private SSZMutableList<PendingAttestationPhase1> currentEpochAttestations;

  MutableBeaconStatePhase1Impl(BeaconStatePhase1Impl backingImmutableView) {
    this(backingImmutableView, false);
  }

  MutableBeaconStatePhase1Impl(BeaconStatePhase1Impl backingImmutableView, boolean builder) {
    super(backingImmutableView);
    this.transitionCaches =
        builder ? TransitionCaches.getNoOp() : backingImmutableView.getTransitionCaches().copy();
    this.builder = builder;
  }

  @Override
  protected BeaconStatePhase1Impl createViewRead(
      TreeNode backingNode, IntCache<ViewRead> viewCache) {
    return new BeaconStatePhase1Impl(
        getType(),
        backingNode,
        viewCache,
        builder ? TransitionCaches.createNewEmpty() : transitionCaches);
  }

  @Override
  public TransitionCaches getTransitionCaches() {
    return transitionCaches;
  }

  @Override
  public BeaconStatePhase1 commitChanges() {
    return (BeaconStatePhase1) super.commitChanges();
  }

  @Override
  public Bytes32 hash_tree_root() {
    return hashTreeRoot();
  }

  @Override
  public Bytes32 hashTreeRoot() {
    return commitChanges().hashTreeRoot();
  }

  @Override
  public int getSSZFieldCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public MutableBeaconState createWritableCopy() {
    return (MutableBeaconState) super.createWritableCopy();
  }

  @Override
  public SSZMutableList<ValidatorPhase1> getValidators() {
    return validators != null
        ? validators
        : (validators = MutableBeaconStatePhase1.super.getValidators());
  }

  @Override
  public SSZMutableList<UnsignedLong> getBalances() {
    return balances != null ? balances : (balances = MutableBeaconStatePhase1.super.getBalances());
  }

  @Override
  public SSZMutableVector<Bytes32> getBlock_roots() {
    return blockRoots != null
        ? blockRoots
        : (blockRoots = MutableBeaconStatePhase1.super.getBlock_roots());
  }

  @Override
  public SSZMutableVector<Bytes32> getState_roots() {
    return stateRoots != null
        ? stateRoots
        : (stateRoots = MutableBeaconStatePhase1.super.getState_roots());
  }

  @Override
  public SSZMutableList<Bytes32> getHistorical_roots() {
    return historicalRoots != null
        ? historicalRoots
        : (historicalRoots = MutableBeaconStatePhase1.super.getHistorical_roots());
  }

  @Override
  public SSZMutableList<Eth1Data> getEth1_data_votes() {
    return eth1DataVotes != null
        ? eth1DataVotes
        : (eth1DataVotes = MutableBeaconStatePhase1.super.getEth1_data_votes());
  }

  @Override
  public SSZMutableVector<Bytes32> getRandao_mixes() {
    return randaoMixes != null
        ? randaoMixes
        : (randaoMixes = MutableBeaconStatePhase1.super.getRandao_mixes());
  }

  @Override
  public SSZMutableList<PendingAttestationPhase1> getPrevious_epoch_attestations() {
    return previousEpochAttestations != null
        ? previousEpochAttestations
        : (previousEpochAttestations =
            MutableBeaconStatePhase1.super.getPrevious_epoch_attestations());
  }

  @Override
  public SSZMutableList<PendingAttestationPhase1> getCurrent_epoch_attestations() {
    return currentEpochAttestations != null
        ? currentEpochAttestations
        : (currentEpochAttestations =
            MutableBeaconStatePhase1.super.getCurrent_epoch_attestations());
  }

  @Override
  public String toString() {
    return BeaconStatePhase1Impl.toString(this);
  }

  @Override
  public boolean equals(Object obj) {
    return BeaconStatePhase1Impl.equals(this, obj);
  }

  @Override
  public int hashCode() {
    return BeaconStatePhase1Impl.hashCode(this);
  }

  @Override
  public <E1 extends Exception, E2 extends Exception, E3 extends Exception>
      BeaconStatePhase1 updated(Mutator<E1, E2, E3> mutator) {
    throw new UnsupportedOperationException();
  }
}
