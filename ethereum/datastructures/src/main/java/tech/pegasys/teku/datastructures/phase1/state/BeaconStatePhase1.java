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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader;
import tech.pegasys.teku.datastructures.blocks.Eth1Data;
import tech.pegasys.teku.datastructures.phase1.config.ConstantsPhase1;
import tech.pegasys.teku.datastructures.phase1.shard.ShardState;
import tech.pegasys.teku.datastructures.state.Checkpoint;
import tech.pegasys.teku.datastructures.state.Fork;
import tech.pegasys.teku.datastructures.state.ForkInfo;
import tech.pegasys.teku.ssz.SSZTypes.Bitvector;
import tech.pegasys.teku.ssz.SSZTypes.SSZBackingList;
import tech.pegasys.teku.ssz.SSZTypes.SSZBackingVector;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.SSZTypes.SSZVector;
import tech.pegasys.teku.ssz.backing.ContainerViewRead;
import tech.pegasys.teku.ssz.backing.ViewWrite;
import tech.pegasys.teku.ssz.backing.type.BasicViewTypes;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.type.ListViewType;
import tech.pegasys.teku.ssz.backing.type.VectorViewType;
import tech.pegasys.teku.ssz.backing.view.AbstractBasicView;
import tech.pegasys.teku.ssz.backing.view.BasicViews.ByteView;
import tech.pegasys.teku.ssz.backing.view.BasicViews.Bytes32View;
import tech.pegasys.teku.ssz.backing.view.BasicViews.UInt64View;
import tech.pegasys.teku.ssz.backing.view.ViewUtils;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.config.Constants;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public interface BeaconStatePhase1
    extends ContainerViewRead, Merkleizable, SimpleOffsetSerializable, SSZContainer {

  Field GENESIS_TIME_FIELD = new Field(0, BasicViewTypes.UINT64_TYPE);
  Field GENESIS_VALIDATORS_ROOT_FIELD = new Field(1, BasicViewTypes.BYTES32_TYPE);
  Field SLOT_FIELD = new Field(2, BasicViewTypes.UINT64_TYPE);
  Field FORK_FIELD = new Field(3, Fork.TYPE);
  Field LATEST_BLOCK_HEADER_FIELD = new Field(4, BeaconBlockHeader.TYPE);
  Field BLOCK_ROOTS_FIELD =
      new Field(
          5,
          () ->
              new VectorViewType<>(
                  BasicViewTypes.BYTES32_TYPE, Constants.SLOTS_PER_HISTORICAL_ROOT));
  Field STATE_ROOTS_FIELD =
      new Field(
          6,
          () ->
              new VectorViewType<>(
                  BasicViewTypes.BYTES32_TYPE, Constants.SLOTS_PER_HISTORICAL_ROOT));
  Field HISTORICAL_ROOTS_FIELD =
      new Field(
          7,
          () -> new ListViewType<>(BasicViewTypes.BYTES32_TYPE, Constants.HISTORICAL_ROOTS_LIMIT));
  Field ETH1_DATA_FIELD = new Field(8, Eth1Data.TYPE);
  Field ETH1_DATA_VOTES_FIELD =
      new Field(
          9,
          () ->
              new ListViewType<>(
                  Eth1Data.TYPE,
                  Constants.EPOCHS_PER_ETH1_VOTING_PERIOD * Constants.SLOTS_PER_EPOCH));
  Field ETH1_DEPOSIT_INDEX_FIELD = new Field(10, BasicViewTypes.UINT64_TYPE);
  Field VALIDATORS_FIELD =
      new Field(
          11, () -> new ListViewType<>(ValidatorPhase1.TYPE, Constants.VALIDATOR_REGISTRY_LIMIT));
  Field BALANCES_FIELD =
      new Field(
          12,
          () -> new ListViewType<>(BasicViewTypes.UINT64_TYPE, Constants.VALIDATOR_REGISTRY_LIMIT));
  Field RANDAO_MIXES_FIELD =
      new Field(
          13,
          () ->
              new VectorViewType<>(
                  BasicViewTypes.BYTES32_TYPE, Constants.EPOCHS_PER_HISTORICAL_VECTOR));
  Field SLASHINGS_FIELD =
      new Field(
          14,
          () ->
              new VectorViewType<>(
                  BasicViewTypes.UINT64_TYPE, Constants.EPOCHS_PER_SLASHINGS_VECTOR));
  Field PREVIOUS_EPOCH_ATTESTATIONS_FIELD =
      new Field(
          15,
          () ->
              new ListViewType<>(
                  PendingAttestationPhase1.TYPE,
                  Constants.MAX_ATTESTATIONS * Constants.SLOTS_PER_EPOCH));
  Field CURRENT_EPOCH_ATTESTATIONS_FIELD =
      new Field(
          16,
          () ->
              new ListViewType<>(
                  PendingAttestationPhase1.TYPE,
                  Constants.MAX_ATTESTATIONS * Constants.SLOTS_PER_EPOCH));
  Field JUSTIFICATION_BITS_FIELD =
      new Field(
          17,
          () -> new VectorViewType<>(BasicViewTypes.BIT_TYPE, Constants.JUSTIFICATION_BITS_LENGTH));
  Field PREVIOUS_JUSTIFIED_CHECKPOINT_FIELD = new Field(18, Checkpoint.TYPE);
  Field CURRENT_JUSTIFIED_CHECKPOINT_FIELD = new Field(19, Checkpoint.TYPE);
  Field FINALIZED_CHECKPOINT_FIELD = new Field(20, Checkpoint.TYPE);
  Field CURRENT_EPOCH_START_SHARD = new Field(21, BasicViewTypes.UINT64_TYPE);
  Field SHARD_STATE_FIELD =
      new Field(22, () -> new ListViewType<>(ShardState.TYPE, ConstantsPhase1.MAX_SHARDS));
  Field ONLINE_COUNTDOWN_FIELD =
      new Field(
          23,
          () -> new ListViewType<>(BasicViewTypes.BYTE_TYPE, Constants.VALIDATOR_REGISTRY_LIMIT));
  Field CURRENT_LIGHT_COMMITTEE_FIELD = new Field(24, CompactCommittee.TYPE);
  Field NEXT_LIGHT_COMMITTEE_FIELD = new Field(25, CompactCommittee.TYPE);
  Field EXPOSED_DERIVED_SECRETS_FIELD =
      new Field(
          26,
          () ->
              new VectorViewType<ExposedValidatorIndices>(
                  ExposedValidatorIndices.TYPE,
                  ConstantsPhase1.EARLY_DERIVED_SECRET_PENALTY_MAX_FUTURE_EPOCHS));

  static ContainerViewType<BeaconStatePhase1> getSSZType() {
    return new ContainerViewType<>(
        SSZContainer.listFields(BeaconStatePhase1.class).stream()
            .map(f -> f.getViewType().get())
            .collect(Collectors.toList()),
        BeaconStatePhase1Impl::new);
  }

  static BeaconStatePhase1 createEmpty() {
    return new BeaconStatePhase1Impl();
  }

  static BeaconStatePhase1 create(

      // Versioning
      UnsignedLong genesis_time,
      Bytes32 genesis_validators_root,
      UnsignedLong slot,
      Fork fork,

      // History
      BeaconBlockHeader latest_block_header,
      SSZVector<Bytes32> block_roots,
      SSZVector<Bytes32> state_roots,
      SSZList<Bytes32> historical_roots,

      // Eth1
      Eth1Data eth1_data,
      SSZList<Eth1Data> eth1_data_votes,
      UnsignedLong eth1_deposit_index,

      // Registry
      SSZList<? extends ValidatorPhase1> validators,
      SSZList<UnsignedLong> balances,

      // Randomness
      SSZVector<Bytes32> randao_mixes,

      // Slashings
      SSZVector<UnsignedLong> slashings,

      // Attestations
      SSZList<PendingAttestationPhase1> previous_epoch_attestations,
      SSZList<PendingAttestationPhase1> current_epoch_attestations,

      // Finality
      Bitvector justification_bits,
      Checkpoint previous_justified_checkpoint,
      Checkpoint current_justified_checkpoint,
      Checkpoint finalized_checkpoint,

      // Phase 1
      UnsignedLong current_epoch_start_shard,
      SSZList<ShardState> shard_states,
      SSZList<Byte> online_countdown,
      CompactCommittee current_light_committee,
      CompactCommittee next_light_committee,
      SSZVector<ExposedValidatorIndices> exposed_derived_secrets) {

    return createEmpty()
        .updated(
            state -> {
              state.setGenesis_time(genesis_time);
              state.setGenesis_validators_root(genesis_validators_root);
              state.setSlot(slot);
              state.setFork(fork);
              state.setLatest_block_header(latest_block_header);
              state.getBlock_roots().setAll(block_roots);
              state.getState_roots().setAll(state_roots);
              state.getHistorical_roots().setAll(historical_roots);
              state.setEth1_data(eth1_data);
              state.getEth1_data_votes().setAll(eth1_data_votes);
              state.setEth1_deposit_index(eth1_deposit_index);
              state.getValidators().setAll(validators);
              state.getBalances().setAll(balances);
              state.getRandao_mixes().setAll(randao_mixes);
              state.getSlashings().setAll(slashings);
              state.getPrevious_epoch_attestations().setAll(previous_epoch_attestations);
              state.getCurrent_epoch_attestations().setAll(current_epoch_attestations);
              state.setJustification_bits(justification_bits);
              state.setPrevious_justified_checkpoint(previous_justified_checkpoint);
              state.setCurrent_justified_checkpoint(current_justified_checkpoint);
              state.setFinalized_checkpoint(finalized_checkpoint);
              state.setCurrent_epoch_start_shard(current_epoch_start_shard);
              state.getShard_states().setAll(shard_states);
              state.getOnline_countdown().setAll(online_countdown);
              state.setCurrent_light_committee(current_light_committee);
              state.setNext_light_committee(next_light_committee);
              state.getExposed_derived_secrets().setAll(exposed_derived_secrets);
            });
  }

  // Versioning
  default UnsignedLong getGenesis_time() {
    return ((UInt64View) get(GENESIS_TIME_FIELD.getIndex())).get();
  }

  default Bytes32 getGenesis_validators_root() {
    return ((Bytes32View) get(GENESIS_VALIDATORS_ROOT_FIELD.getIndex())).get();
  }

  default UnsignedLong getSlot() {
    return ((UInt64View) get(SLOT_FIELD.getIndex())).get();
  }

  default Fork getFork() {
    return getAny(FORK_FIELD.getIndex());
  }

  default ForkInfo getForkInfo() {
    return new ForkInfo(getFork(), getGenesis_validators_root());
  }

  // History
  default BeaconBlockHeader getLatest_block_header() {
    return getAny(LATEST_BLOCK_HEADER_FIELD.getIndex());
  }

  default SSZVector<Bytes32> getBlock_roots() {
    return new SSZBackingVector<>(
        Bytes32.class,
        getAny(BLOCK_ROOTS_FIELD.getIndex()),
        Bytes32View::new,
        AbstractBasicView::get);
  }

  default SSZVector<Bytes32> getState_roots() {
    return new SSZBackingVector<>(
        Bytes32.class,
        getAny(STATE_ROOTS_FIELD.getIndex()),
        Bytes32View::new,
        AbstractBasicView::get);
  }

  default SSZList<Bytes32> getHistorical_roots() {
    return new SSZBackingList<>(
        Bytes32.class,
        getAny(HISTORICAL_ROOTS_FIELD.getIndex()),
        Bytes32View::new,
        AbstractBasicView::get);
  }

  // Eth1
  default Eth1Data getEth1_data() {
    return getAny(ETH1_DATA_FIELD.getIndex());
  }

  default SSZList<Eth1Data> getEth1_data_votes() {
    return new SSZBackingList<>(
        Eth1Data.class,
        getAny(ETH1_DATA_VOTES_FIELD.getIndex()),
        Function.identity(),
        Function.identity());
  }

  default UnsignedLong getEth1_deposit_index() {
    return ((UInt64View) get(ETH1_DEPOSIT_INDEX_FIELD.getIndex())).get();
  }

  // Registry
  default SSZList<ValidatorPhase1> getValidators() {
    return new SSZBackingList<>(
        ValidatorPhase1.class,
        getAny(VALIDATORS_FIELD.getIndex()),
        Function.identity(),
        Function.identity());
  }

  default SSZList<UnsignedLong> getBalances() {
    return new SSZBackingList<>(
        UnsignedLong.class,
        getAny(BALANCES_FIELD.getIndex()),
        UInt64View::new,
        AbstractBasicView::get);
  }

  default SSZVector<Bytes32> getRandao_mixes() {
    return new SSZBackingVector<>(
        Bytes32.class,
        getAny(RANDAO_MIXES_FIELD.getIndex()),
        Bytes32View::new,
        AbstractBasicView::get);
  }

  // Slashings
  default SSZVector<UnsignedLong> getSlashings() {
    return new SSZBackingVector<>(
        UnsignedLong.class,
        getAny(SLASHINGS_FIELD.getIndex()),
        UInt64View::new,
        AbstractBasicView::get);
  }

  // Attestations
  default SSZList<PendingAttestationPhase1> getPrevious_epoch_attestations() {
    return new SSZBackingList<>(
        PendingAttestationPhase1.class,
        getAny(PREVIOUS_EPOCH_ATTESTATIONS_FIELD.getIndex()),
        Function.identity(),
        Function.identity());
  }

  default SSZList<PendingAttestationPhase1> getCurrent_epoch_attestations() {
    return new SSZBackingList<>(
        PendingAttestationPhase1.class,
        getAny(CURRENT_EPOCH_ATTESTATIONS_FIELD.getIndex()),
        Function.identity(),
        Function.identity());
  }

  // Finality
  default Bitvector getJustification_bits() {
    return ViewUtils.getBitvector(getAny(JUSTIFICATION_BITS_FIELD.getIndex()));
  }

  default Checkpoint getPrevious_justified_checkpoint() {
    return getAny(PREVIOUS_JUSTIFIED_CHECKPOINT_FIELD.getIndex());
  }

  default Checkpoint getCurrent_justified_checkpoint() {
    return getAny(CURRENT_JUSTIFIED_CHECKPOINT_FIELD.getIndex());
  }

  default Checkpoint getFinalized_checkpoint() {
    return getAny(FINALIZED_CHECKPOINT_FIELD.getIndex());
  }

  default UnsignedLong getCurrent_epoch_start_shard() {
    return getAny(CURRENT_EPOCH_START_SHARD.getIndex());
  }

  default SSZList<ShardState> getShard_states() {
    return new SSZBackingList<>(
        ShardState.class,
        getAny(SHARD_STATE_FIELD.getIndex()),
        Function.identity(),
        Function.identity());
  }

  default SSZList<Byte> getOnline_countdown() {
    return new SSZBackingList<>(
        Byte.class,
        getAny(ONLINE_COUNTDOWN_FIELD.getIndex()),
        ByteView::new,
        AbstractBasicView::get);
  }

  default CompactCommittee getCurrent_light_committee() {
    return getAny(CURRENT_LIGHT_COMMITTEE_FIELD.getIndex());
  }

  default CompactCommittee getNext_light_committee() {
    return getAny(NEXT_LIGHT_COMMITTEE_FIELD.getIndex());
  }

  default SSZVector<ExposedValidatorIndices> getExposed_derived_secrets() {
    return new SSZBackingVector<>(
        ExposedValidatorIndices.class,
        getAny(EXPOSED_DERIVED_SECRETS_FIELD.getIndex()),
        Function.identity(),
        Function.identity());
  }

  @Override
  default ViewWrite createWritableCopy() {
    throw new UnsupportedOperationException("Use BeaconState.updated() to modify");
  }

  <E1 extends Exception, E2 extends Exception, E3 extends Exception> BeaconStatePhase1 updated(
      Mutator<E1, E2, E3> mutator) throws E1, E2, E3;

  interface Mutator<E1 extends Exception, E2 extends Exception, E3 extends Exception> {
    void mutate(MutableBeaconStatePhase1 state) throws E1, E2, E3;
  }
}
