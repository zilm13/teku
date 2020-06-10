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

package tech.pegasys.teku.phase1.datastructures.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.datastructures.blocks.Eth1Data;
import tech.pegasys.teku.datastructures.operations.Deposit;
import tech.pegasys.teku.datastructures.operations.ProposerSlashing;
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit;
import tech.pegasys.teku.datastructures.util.SimpleOffsetSerializer;
import tech.pegasys.teku.phase1.datastructures.operations.AttestationPhase1;
import tech.pegasys.teku.phase1.datastructures.operations.AttesterSlashingPhase1;
import tech.pegasys.teku.phase1.datastructures.operations.CustodyKeyReveal;
import tech.pegasys.teku.phase1.datastructures.operations.EarlyDerivedSecretReveal;
import tech.pegasys.teku.phase1.datastructures.operations.SignedCustodySlashing;
import tech.pegasys.teku.phase1.datastructures.shard.ShardTransition;
import tech.pegasys.teku.ssz.SSZTypes.Bitvector;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.HashTreeUtil.SSZTypes;

/** A Beacon block body */
public class BeaconBlockBodyPhase1 implements SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  public static final int SSZ_FIELD_COUNT = 11;

  private final BLSSignature randao_reveal;
  private final Eth1Data eth1_data;
  private final Bytes32 graffiti;
  private final SSZList<ProposerSlashing>
      proposer_slashings; // List bounded by MAX_PROPOSER_SLASHINGS
  private final SSZList<AttesterSlashingPhase1>
      attester_slashings; // List bounded by MAX_ATTESTER_SLASHINGS
  private final SSZList<AttestationPhase1> attestations; // List bounded by MAX_ATTESTATIONS
  private final SSZList<Deposit> deposits; // List bounded by MAX_DEPOSITS
  private final SSZList<SignedVoluntaryExit> voluntary_exits; // List bounded by MAX_VOLUNTARY_EXITS
  private final SSZList<SignedCustodySlashing>
      custody_slashings; // List bounded by MAX_CUSTODY_SLASHINGS
  private final SSZList<CustodyKeyReveal>
      custody_key_reveals; // List bounded by MAX_CUSTODY_KEY_REVEALS
  private final SSZList<EarlyDerivedSecretReveal>
      early_derived_secret_reveals; // List bounded by MAX_EARLY_DERIVED_SECRET_REVEALS
  private final SSZList<ShardTransition> shard_transitions; // List bounded by MAX_SHARDS
  private final Bitvector
      light_client_signature_bitfield; // Vector size is LIGHT_CLIENT_COMMITTEE_SIZE
  private final BLSSignature light_client_signature;

  public BeaconBlockBodyPhase1(
      BLSSignature randao_reveal,
      Eth1Data eth1_data,
      Bytes32 graffiti,
      SSZList<ProposerSlashing> proposer_slashings,
      SSZList<AttesterSlashingPhase1> attester_slashings,
      SSZList<AttestationPhase1> attestations,
      SSZList<Deposit> deposits,
      SSZList<SignedVoluntaryExit> voluntary_exits,
      SSZList<SignedCustodySlashing> custody_slashings,
      SSZList<CustodyKeyReveal> custody_key_reveals,
      SSZList<EarlyDerivedSecretReveal> early_derived_secret_reveals,
      SSZList<ShardTransition> shard_transitions,
      Bitvector light_client_signature_bitfield,
      BLSSignature light_client_signature) {
    this.randao_reveal = randao_reveal;
    this.eth1_data = eth1_data;
    this.graffiti = graffiti;
    this.proposer_slashings = proposer_slashings;
    this.attester_slashings = attester_slashings;
    this.attestations = attestations;
    this.deposits = deposits;
    this.voluntary_exits = voluntary_exits;
    this.custody_slashings = custody_slashings;
    this.custody_key_reveals = custody_key_reveals;
    this.early_derived_secret_reveals = early_derived_secret_reveals;
    this.shard_transitions = shard_transitions;
    this.light_client_signature_bitfield = light_client_signature_bitfield;
    this.light_client_signature = light_client_signature;
  }

  @Override
  public int getSSZFieldCount() {
    return randao_reveal.getSSZFieldCount()
        + eth1_data.getSSZFieldCount()
        + SSZ_FIELD_COUNT
        + light_client_signature.getSSZFieldCount();
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList = new ArrayList<>();
    fixedPartsList.addAll(randao_reveal.get_fixed_parts());
    fixedPartsList.addAll(eth1_data.get_fixed_parts());
    fixedPartsList.addAll(List.of(SSZ.encode(writer -> writer.writeFixedBytes(graffiti))));
    fixedPartsList.addAll(Collections.nCopies(9, Bytes.EMPTY));
    fixedPartsList.addAll(List.of(light_client_signature_bitfield.serialize()));
    fixedPartsList.addAll(light_client_signature.get_fixed_parts());
    return fixedPartsList;
  }

  @Override
  public List<Bytes> get_variable_parts() {
    List<Bytes> variablePartsList = new ArrayList<>();
    variablePartsList.addAll(Collections.nCopies(randao_reveal.getSSZFieldCount(), Bytes.EMPTY));
    variablePartsList.addAll(Collections.nCopies(eth1_data.getSSZFieldCount(), Bytes.EMPTY));
    variablePartsList.addAll(List.of(Bytes.EMPTY));
    variablePartsList.addAll(
        List.of(
            SimpleOffsetSerializer.serializeFixedCompositeList(proposer_slashings),
            SimpleOffsetSerializer.serializeVariableCompositeList(attester_slashings),
            SimpleOffsetSerializer.serializeVariableCompositeList(attestations),
            SimpleOffsetSerializer.serializeFixedCompositeList(deposits),
            SimpleOffsetSerializer.serializeFixedCompositeList(voluntary_exits),
            SimpleOffsetSerializer.serializeVariableCompositeList(custody_slashings),
            SimpleOffsetSerializer.serializeFixedCompositeList(custody_key_reveals),
            SimpleOffsetSerializer.serializeFixedCompositeList(early_derived_secret_reveals),
            SimpleOffsetSerializer.serializeVariableCompositeList(shard_transitions),
            Bytes.EMPTY,
            Bytes.EMPTY));
    return variablePartsList;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BeaconBlockBodyPhase1)) {
      return false;
    }
    BeaconBlockBodyPhase1 that = (BeaconBlockBodyPhase1) o;
    return Objects.equals(randao_reveal, that.randao_reveal)
        && Objects.equals(eth1_data, that.eth1_data)
        && Objects.equals(graffiti, that.graffiti)
        && Objects.equals(proposer_slashings, that.proposer_slashings)
        && Objects.equals(attester_slashings, that.attester_slashings)
        && Objects.equals(attestations, that.attestations)
        && Objects.equals(deposits, that.deposits)
        && Objects.equals(voluntary_exits, that.voluntary_exits)
        && Objects.equals(custody_slashings, that.custody_slashings)
        && Objects.equals(custody_key_reveals, that.custody_key_reveals)
        && Objects.equals(early_derived_secret_reveals, that.early_derived_secret_reveals)
        && Objects.equals(shard_transitions, that.shard_transitions)
        && Objects.equals(light_client_signature_bitfield, that.light_client_signature_bitfield)
        && Objects.equals(light_client_signature, that.light_client_signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        randao_reveal,
        eth1_data,
        graffiti,
        proposer_slashings,
        attester_slashings,
        attestations,
        deposits,
        voluntary_exits,
        custody_slashings,
        custody_key_reveals,
        early_derived_secret_reveals,
        shard_transitions,
        light_client_signature_bitfield,
        light_client_signature);
  }

  /** ******************* * GETTERS & SETTERS * * ******************* */
  public BLSSignature getRandao_reveal() {
    return randao_reveal;
  }

  public Eth1Data getEth1_data() {
    return eth1_data;
  }

  public Bytes32 getGraffiti() {
    return graffiti;
  }

  public SSZList<AttestationPhase1> getAttestations() {
    return attestations;
  }

  public SSZList<ProposerSlashing> getProposer_slashings() {
    return proposer_slashings;
  }

  public SSZList<AttesterSlashingPhase1> getAttester_slashings() {
    return attester_slashings;
  }

  public SSZList<Deposit> getDeposits() {
    return deposits;
  }

  public SSZList<SignedVoluntaryExit> getVoluntary_exits() {
    return voluntary_exits;
  }

  public SSZList<SignedCustodySlashing> getCustody_slashings() {
    return custody_slashings;
  }

  public SSZList<CustodyKeyReveal> getCustody_key_reveals() {
    return custody_key_reveals;
  }

  public SSZList<EarlyDerivedSecretReveal> getEarly_derived_secret_reveals() {
    return early_derived_secret_reveals;
  }

  public SSZList<ShardTransition> getShard_transitions() {
    return shard_transitions;
  }

  public Bitvector getLight_client_signature_bitfield() {
    return light_client_signature_bitfield;
  }

  public BLSSignature getLight_client_signature() {
    return light_client_signature;
  }

  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, randao_reveal.toBytes()),
            eth1_data.hash_tree_root(),
            HashTreeUtil.hash_tree_root(SSZTypes.VECTOR_OF_BASIC, graffiti),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, proposer_slashings),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, attester_slashings),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, attestations),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, deposits),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, voluntary_exits),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, custody_slashings),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, custody_key_reveals),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, early_derived_secret_reveals),
            HashTreeUtil.hash_tree_root(SSZTypes.LIST_OF_COMPOSITE, shard_transitions),
            HashTreeUtil.hash_tree_root_bitvector(light_client_signature_bitfield),
            HashTreeUtil.hash_tree_root(
                SSZTypes.VECTOR_OF_BASIC, light_client_signature.toBytes())));
  }
}
