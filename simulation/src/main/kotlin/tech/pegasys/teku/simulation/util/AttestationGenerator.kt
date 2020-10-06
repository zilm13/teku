package tech.pegasys.teku.simulation.util

import com.google.common.base.Preconditions
import com.google.common.primitives.UnsignedLong
import tech.pegasys.teku.bls.BLS
import tech.pegasys.teku.bls.BLSKeyPair
import tech.pegasys.teku.bls.BLSSignature
import tech.pegasys.teku.core.CommitteeAssignmentUtil
import tech.pegasys.teku.core.signatures.LocalMessageSignerService
import tech.pegasys.teku.core.signatures.Signer
import tech.pegasys.teku.datastructures.blocks.BeaconBlock
import tech.pegasys.teku.datastructures.blocks.BeaconBlockAndState
import tech.pegasys.teku.datastructures.operations.Attestation
import tech.pegasys.teku.datastructures.operations.AttestationData
import tech.pegasys.teku.datastructures.state.BeaconState
import tech.pegasys.teku.datastructures.state.Committee
import tech.pegasys.teku.datastructures.util.AttestationUtil
import tech.pegasys.teku.datastructures.util.BeaconStateUtil
import tech.pegasys.teku.ssz.SSZTypes.Bitlist
import tech.pegasys.teku.util.config.Constants
import java.security.SecureRandom
import java.util.ArrayList
import java.util.Collections
import java.util.Optional
import java.util.Spliterator
import java.util.Spliterators
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport


class AttestationGenerator(private val validatorKeys: List<BLSKeyPair>, rnd: SecureRandom) {
    private val randomKeyPair = BLSKeyPair.random(rnd)

    @JvmOverloads
    fun validAttestation(
            blockAndState: BeaconBlockAndState, slot: UnsignedLong = blockAndState.slot): Attestation {
        return createAttestation(blockAndState, true, slot)
    }

    fun attestationWithInvalidSignature(blockAndState: BeaconBlockAndState): Attestation {
        return createAttestation(blockAndState, false, blockAndState.slot)
    }

    private fun createAttestation(
            blockAndState: BeaconBlockAndState,
            withValidSignature: Boolean,
            slot: UnsignedLong): Attestation {
        var assignedSlot = slot
        var attestation: Optional<Attestation> = Optional.empty()
        while (attestation.isEmpty) {
            val attestations = if (withValidSignature) streamAttestations(blockAndState, assignedSlot) else streamInvalidAttestations(blockAndState, assignedSlot)
            attestation = attestations.findFirst()
            assignedSlot = assignedSlot.plus(UnsignedLong.ONE)
        }
        return attestation.orElseThrow()
    }

    fun getAttestationsForSlot(blockAndState: BeaconBlockAndState): List<Attestation> {
        return getAttestationsForSlot(
                blockAndState.state, blockAndState.block, blockAndState.slot)
    }

    fun getAttestationsForSlot(
            state: BeaconState, block: BeaconBlock, slot: UnsignedLong): List<Attestation> {
        return streamAttestations(BeaconBlockAndState(block, state), slot)
                .collect(Collectors.toList())
    }

    /**
     * Streams attestations for validators assigned to attest at `assignedSlot`, using the given
     * `headBlockAndState` as the calculated chain head.
     *
     * @param headBlockAndState The chain head to attest to
     * @param assignedSlot The assigned slot for which to produce attestations
     * @return A stream of valid attestations to produce at the assigned slot
     */
    fun streamAttestations(
            headBlockAndState: BeaconBlockAndState, assignedSlot: UnsignedLong): Stream<Attestation> {
        return AttestationIterator.create(headBlockAndState, assignedSlot, validatorKeys).toStream()
    }

    /**
     * Streams invalid attestations for validators assigned to attest at `assignedSlot`, using
     * the given `headBlockAndState` as the calculated chain head.
     *
     * @param headBlockAndState The chain head to attest to
     * @param assignedSlot The assigned slot for which to produce attestations
     * @return A stream of invalid attestations produced at the assigned slot
     */
    private fun streamInvalidAttestations(
            headBlockAndState: BeaconBlockAndState, assignedSlot: UnsignedLong): Stream<Attestation> {
        return AttestationIterator.createWithInvalidSignatures(
                headBlockAndState, assignedSlot, validatorKeys, randomKeyPair)
                .toStream()
    }

    /**
     * Iterates through valid attestations with the supplied head block, produced at the given
     * assigned slot.
     */
    private class AttestationIterator private constructor(
            // The head block to attest to with its corresponding state
            private val headBlockAndState: BeaconBlockAndState,
            // The assigned slot to generate attestations for
            private val assignedSlot: UnsignedLong,
            // Validator keys
            private val validatorKeys: List<BLSKeyPair>,
            validatorKeySupplier: Function<Int, BLSKeyPair>) : Iterator<Attestation> {

        // The epoch containing the assigned slot
        private val assignedSlotEpoch: UnsignedLong

        private val validatorKeySupplier: Function<Int, BLSKeyPair>
        private var nextAttestation: Optional<Attestation> = Optional.empty()
        private var currentValidatorIndex = 0
        fun toStream(): Stream<Attestation> {
            val split: Spliterator<Attestation> = Spliterators.spliteratorUnknownSize(
                    this, Spliterator.IMMUTABLE or Spliterator.DISTINCT or Spliterator.NONNULL)
            return StreamSupport.stream(split, false)
        }

        override fun hasNext(): Boolean {
            return nextAttestation.isPresent
        }

        override fun next(): Attestation {
            if (nextAttestation.isEmpty) {
                throw NoSuchElementException()
            }
            val attestation = nextAttestation.get()
            generateNextAttestation()
            return attestation
        }

        private fun generateNextAttestation() {
            nextAttestation = Optional.empty()
            val headState = headBlockAndState.state
            val headBlock = headBlockAndState.block
            var lastProcessedValidatorIndex = currentValidatorIndex
            for (validatorIndex in currentValidatorIndex until validatorKeys.size) {
                lastProcessedValidatorIndex = validatorIndex
                val maybeAssignment = CommitteeAssignmentUtil.get_committee_assignment(
                        headState, assignedSlotEpoch, validatorIndex)
                if (maybeAssignment.isEmpty) {
                    continue
                }
                val assignment = maybeAssignment.get()
                if (assignment.slot != assignedSlot) {
                    continue
                }
                val committeeIndices = assignment.committee
                val committeeIndex = assignment.committeeIndex
                val committee = Committee(committeeIndex, committeeIndices)
                val indexIntoCommittee = committeeIndices.indexOf(validatorIndex)
                val genericAttestationData = AttestationUtil.getGenericAttestationData(
                        assignedSlot, headState, headBlock, committeeIndex)
                val validatorKeyPair = validatorKeySupplier.apply(validatorIndex)
                nextAttestation = Optional.of(
                        createAttestation(
                                headState,
                                validatorKeyPair,
                                indexIntoCommittee,
                                committee,
                                genericAttestationData))
                break
            }
            currentValidatorIndex = lastProcessedValidatorIndex + 1
        }

        private fun createAttestation(
                state: BeaconState,
                attesterKeyPair: BLSKeyPair,
                indexIntoCommittee: Int,
                committee: Committee,
                attestationData: AttestationData): Attestation {
            val committeSize = committee.committeeSize
            val aggregationBitfield = AttestationUtil.getAggregationBits(committeSize, indexIntoCommittee)
            val signature = Signer(LocalMessageSignerService(attesterKeyPair))
                    .signAttestationData(attestationData, state.forkInfo)
                    .join()
            return Attestation(aggregationBitfield, attestationData, signature)
        }

        companion object {
            fun create(
                    headBlockAndState: BeaconBlockAndState,
                    assignedSlot: UnsignedLong,
                    validatorKeys: List<BLSKeyPair>): AttestationIterator {
                return AttestationIterator(
                        headBlockAndState, assignedSlot, validatorKeys, Function { i: Int -> validatorKeys[i!!] })
            }

            fun createWithInvalidSignatures(
                    headBlockAndState: BeaconBlockAndState,
                    assignedSlot: UnsignedLong,
                    validatorKeys: List<BLSKeyPair>,
                    invalidKeyPair: BLSKeyPair): AttestationIterator {
                return AttestationIterator(
                        headBlockAndState, assignedSlot, validatorKeys, Function { invalidKeyPair })
            }
        }

        init {
            assignedSlotEpoch = BeaconStateUtil.compute_epoch_at_slot(assignedSlot)
            this.validatorKeySupplier = validatorKeySupplier
            generateNextAttestation()
        }
    }

    companion object {
        fun getSingleAttesterIndex(attestation: Attestation): Int {
            return attestation.aggregation_bits.streamAllSetBits().findFirst().orElse(-1)
        }

        fun diffSlotAttestationData(slot: UnsignedLong, data: AttestationData): AttestationData {
            return AttestationData(
                    slot, data.index, data.beacon_block_root, data.source, data.target)
        }

        fun withNewSingleAttesterBit(oldAttestation: Attestation): Attestation {
            val attestation = Attestation(oldAttestation)
            val newBitlist = Bitlist(
                    attestation.aggregation_bits.currentSize,
                    attestation.aggregation_bits.maxSize)
            val unsetBits: MutableList<Int> = ArrayList()
            for (i in 0 until attestation.aggregation_bits.currentSize) {
                if (!attestation.aggregation_bits.getBit(i)) {
                    unsetBits.add(i)
                }
            }
            Collections.shuffle(unsetBits)
            newBitlist.setBit(unsetBits[0]!!)
            attestation.aggregation_bits = newBitlist
            return attestation
        }

        /**
         * Groups passed attestations by their [ ] and aggregates attestations in
         * every group to a single [Attestation]
         *
         * @return a list of aggregated [Attestation]s with distinct [     ]
         */
        fun groupAndAggregateAttestations(srcAttestations: List<Attestation>): List<Attestation> {
            val groupedAtt: Collection<List<Attestation>> = srcAttestations.stream().collect(Collectors.groupingBy(Function<Attestation, AttestationData> { obj: Attestation -> obj.data })).values
            return groupedAtt.stream()
                    .map { srcAttestations: List<Attestation> -> aggregateAttestations(srcAttestations) }
                    .collect(Collectors.toList())
        }

        /**
         * Aggregates passed attestations
         *
         * @param srcAttestations attestations which should have the same [Attestation.getData]
         */
        fun aggregateAttestations(srcAttestations: List<Attestation>): Attestation {
            Preconditions.checkArgument(!srcAttestations.isEmpty(), "Expected at least one attestation")
            val targetBitlistSize = srcAttestations.stream()
                    .mapToInt { a: Attestation -> a.aggregation_bits.currentSize }
                    .max()
                    .asInt
            val targetBitlist = Bitlist(targetBitlistSize, Constants.MAX_VALIDATORS_PER_COMMITTEE.toLong())
            srcAttestations.forEach(Consumer { a: Attestation -> targetBitlist.setAllBits(a.aggregation_bits) })
            val targetSig: BLSSignature = BLS.aggregate(
                    srcAttestations.stream()
                            .map { obj: Attestation -> obj.aggregate_signature }
                            )
            return Attestation(targetBitlist, srcAttestations[0].data, targetSig)
        }
    }

}
