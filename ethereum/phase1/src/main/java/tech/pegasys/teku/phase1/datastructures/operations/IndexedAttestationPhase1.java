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

package tech.pegasys.teku.phase1.datastructures.operations;

import com.google.common.primitives.UnsignedLong;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.datastructures.util.SimpleOffsetSerializer;
import tech.pegasys.teku.ssz.SSZTypes.SSZContainer;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.sos.SimpleOffsetSerializable;
import tech.pegasys.teku.util.config.Constants;
import tech.pegasys.teku.util.hashtree.HashTreeUtil;
import tech.pegasys.teku.util.hashtree.Merkleizable;

public class IndexedAttestationPhase1
    implements Merkleizable, SimpleOffsetSerializable, SSZContainer {

  // The number of SimpleSerialize basic types in this SSZ Container/POJO.
  public static final int SSZ_FIELD_COUNT = 1;

  private final SSZList<UnsignedLong> committee; // List bounded by MAX_VALIDATORS_PER_COMMITTEE
  private final AttestationPhase1 attestation;

  public IndexedAttestationPhase1(SSZList<UnsignedLong> committee, AttestationPhase1 attestation) {
    this.committee = committee;
    this.attestation = attestation;
  }

  // Required by SSZ reflection
  public IndexedAttestationPhase1() {
    this.committee =
        SSZList.createMutable(UnsignedLong.class, Constants.MAX_VALIDATORS_PER_COMMITTEE);
    attestation = null;
  }

  public IndexedAttestationPhase1(IndexedAttestationPhase1 indexedAttestation) {
    this.committee = SSZList.createMutable(indexedAttestation.getCommittee());
    this.attestation = indexedAttestation.getAttestation();
  }

  @Override
  public int getSSZFieldCount() {
    return SSZ_FIELD_COUNT + attestation.getSSZFieldCount();
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    return List.of(Bytes.EMPTY, Bytes.EMPTY);
  }

  @Override
  public List<Bytes> get_variable_parts() {
    return List.of(
        // TODO The below lines are a hack while Tuweni SSZ/SOS is being upgraded.
        Bytes.fromHexString(
            committee.stream()
                .map(value -> SSZ.encodeUInt64(value.longValue()).toHexString().substring(2))
                .collect(Collectors.joining())),
        SimpleOffsetSerializer.serialize(attestation));
  }

  @Override
  public int hashCode() {
    return Objects.hash(committee, attestation);
  }

  @Override
  public boolean equals(Object obj) {
    if (Objects.isNull(obj)) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof IndexedAttestationPhase1)) {
      return false;
    }

    IndexedAttestationPhase1 other = (IndexedAttestationPhase1) obj;
    return Objects.equals(this.getCommittee(), other.getCommittee())
        && Objects.equals(this.getAttestation(), other.getAttestation());
  }

  /** ******************* * GETTERS & SETTERS * * ******************* */
  public SSZList<UnsignedLong> getCommittee() {
    return committee;
  }

  public AttestationPhase1 getAttestation() {
    return attestation;
  }

  @Override
  public Bytes32 hash_tree_root() {
    return HashTreeUtil.merkleize(
        Arrays.asList(
            HashTreeUtil.hash_tree_root_list_ul(
                committee.map(Bytes.class, item -> SSZ.encodeUInt64(item.longValue()))),
            attestation.hash_tree_root()));
  }
}
