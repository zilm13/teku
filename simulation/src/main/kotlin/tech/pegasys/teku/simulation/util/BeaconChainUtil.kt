package tech.pegasys.teku.simulation.util

import com.google.common.base.Preconditions
import com.google.common.eventbus.EventBus
import com.google.common.primitives.UnsignedLong
import tech.pegasys.teku.bls.BLSKeyPair
import tech.pegasys.teku.core.results.BlockImportResult
import tech.pegasys.teku.core.signatures.MessageSignerService
import tech.pegasys.teku.datastructures.blocks.Eth1Data
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock
import tech.pegasys.teku.datastructures.blocks.SignedBlockAndState
import tech.pegasys.teku.datastructures.operations.Attestation
import tech.pegasys.teku.datastructures.operations.Deposit
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit
import tech.pegasys.teku.datastructures.util.MockStartValidatorKeyPairFactory
import tech.pegasys.teku.simulation.sync.BlockProposalTestUtil
import tech.pegasys.teku.simulation.sync.TestMessageSignerService
import tech.pegasys.teku.ssz.SSZTypes.SSZList
import tech.pegasys.teku.statetransition.blockimport.BlockImporter
import tech.pegasys.teku.statetransition.util.StartupUtil
import tech.pegasys.teku.storage.client.RecentChainData
import tech.pegasys.teku.util.config.Constants
import java.security.SecureRandom
import java.util.Optional

class BeaconChainUtil private constructor(
        val validatorKeys: List<BLSKeyPair>,
        private val recentChainData: RecentChainData,
        private val signDeposits: Boolean) {
    private val blockCreator: BlockProposalTestUtil = BlockProposalTestUtil()
    private val blockImporter: BlockImporter
    fun setSlot(currentSlot: UnsignedLong) {
        Preconditions.checkState(!recentChainData.isPreGenesis, "Cannot set current slot before genesis")
        val secPerSlot = UnsignedLong.valueOf(Constants.SECONDS_PER_SLOT.toLong())
        val time = recentChainData.genesisTime.plus(currentSlot.times(secPerSlot))
        setTime(time)
    }

    fun setTime(time: UnsignedLong) {
        Preconditions.checkState(!recentChainData.isPreGenesis, "Cannot set time before genesis")
        val tx = recentChainData.startStoreTransaction()
        tx.time = time
        tx.commit().join()
    }

    @Throws(Exception::class)
    fun createAndImportBlockAtSlot(slot: Long): SignedBeaconBlock {
        return createAndImportBlockAtSlot(UnsignedLong.valueOf(slot))
    }

    fun importBlock(block: SignedBeaconBlock) {
        val importResult: BlockImportResult = blockImporter.importBlock(block)
        check(importResult.isSuccessful()) {
            ("Produced an invalid block ( reason "
                    + importResult.getFailureReason().name
                    + ") at slot "
                    + block.slot
                    + ": "
                    + block)
        }
        val tx = recentChainData.startStoreTransaction()
        tx.updateHead()
        tx.commit().join()
        assert(importResult.isSuccessful)
    }

    @Throws(Exception::class)
    fun createAndImportBlockAtSlotWithExits(
            slot: UnsignedLong, exits: List<SignedVoluntaryExit>): SignedBeaconBlock {
        val exitsSSZList: Optional<SSZList<SignedVoluntaryExit>> = if (exits.isEmpty()) Optional.empty() else Optional.of(
                SSZList.createMutable(
                        exits, Constants.MAX_VOLUNTARY_EXITS.toLong(), SignedVoluntaryExit::class.java))
        return createAndImportBlockAtSlot(
                slot, Optional.empty(), Optional.empty(), exitsSSZList, Optional.empty())
    }

    @Throws(Exception::class)
    fun createAndImportBlockAtSlotWithDeposits(
            slot: UnsignedLong, deposits: List<Deposit>): SignedBeaconBlock {
        val depositsSSZlist: Optional<SSZList<Deposit>> = if (deposits.isEmpty()) Optional.empty() else Optional.of(SSZList.createMutable(deposits, Constants.MAX_DEPOSITS.toLong(), Deposit::class.java))
        return createAndImportBlockAtSlot(
                slot, Optional.empty(), depositsSSZlist, Optional.empty(), Optional.empty())
    }

    @Throws(Exception::class)
    fun createAndImportBlockAtSlotWithAttestations(
            slot: UnsignedLong, attestations: List<Attestation>): SignedBeaconBlock {
        val attestationsSSZList: Optional<SSZList<Attestation>> = if (attestations.isEmpty()) Optional.empty() else Optional.of(
                SSZList.createMutable(attestations, Constants.MAX_ATTESTATIONS.toLong(), Attestation::class.java))
        return createAndImportBlockAtSlot(
                slot, attestationsSSZList, Optional.empty(), Optional.empty(), Optional.empty())
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun createAndImportBlockAtSlot(
            slot: UnsignedLong,
            attestations: Optional<SSZList<Attestation>> = Optional.empty(),
            deposits: Optional<SSZList<Deposit>> = Optional.empty(),
            exits: Optional<SSZList<SignedVoluntaryExit>> = Optional.empty(),
            eth1Data: Optional<Eth1Data> = Optional.empty()): SignedBeaconBlock {
        val block = createBlockAndStateAtSlot(slot, true, attestations, deposits, exits, eth1Data).block
        setSlot(slot)
        val importResult: BlockImportResult = blockImporter.importBlock(block)
        check(importResult.isSuccessful()) {
            ("Produced an invalid block ( reason "
                    + importResult.getFailureReason().name
                    + ") at slot "
                    + slot
                    + ": "
                    + block)
        }
        val tx = recentChainData.startStoreTransaction()
        tx.updateHead()
        tx.commit().join()
        return importResult.getBlock()
    }

    @Throws(Exception::class)
    fun createBlockAtSlotFromInvalidProposer(slot: UnsignedLong): SignedBeaconBlock {
        return createBlockAtSlot(slot, false)
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun createBlockAtSlot(slot: UnsignedLong, withValidProposer: Boolean = true): SignedBeaconBlock {
        return createBlockAndStateAtSlot(slot, withValidProposer).block
    }

    @Throws(Exception::class)
    fun createBlockAndStateAtSlot(
            slot: UnsignedLong, withValidProposer: Boolean): SignedBlockAndState {
        return createBlockAndStateAtSlot(
                slot,
                withValidProposer,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty())
    }

    @Throws(Exception::class)
    private fun createBlockAndStateAtSlot(
            slot: UnsignedLong,
            withValidProposer: Boolean,
            attestations: Optional<SSZList<Attestation>>,
            deposits: Optional<SSZList<Deposit>>,
            exits: Optional<SSZList<SignedVoluntaryExit>>,
            eth1Data: Optional<Eth1Data>): SignedBlockAndState {
        Preconditions.checkState(
                withValidProposer || validatorKeys.size > 1,
                "Must have >1 validator in order to create a block from an invalid proposer.")
        val bestBlockRoot = recentChainData.bestBlockRoot.orElseThrow()
        val bestBlock = recentChainData.store.getBlock(bestBlockRoot)
        val preState = recentChainData.bestState.orElseThrow()
        Preconditions.checkArgument(bestBlock.slot.compareTo(slot) < 0, "Slot must be in the future.")
        val correctProposerIndex: Int = blockCreator.getProposerIndexForSlot(preState, slot)
        val proposerIndex = if (withValidProposer) correctProposerIndex else getWrongProposerIndex(correctProposerIndex)
        val signer: MessageSignerService = getSigner(proposerIndex)
        return blockCreator.createBlock(
                signer, slot, preState, bestBlockRoot, attestations, deposits, exits, eth1Data)
    }

    @Throws(Exception::class)
    fun finalizeChainAtEpoch(epoch: UnsignedLong, rnd: SecureRandom) {
        if (recentChainData.store.finalizedCheckpoint.epoch.compareTo(epoch) >= 0) {
            throw Exception("Chain already finalized at this or higher epoch")
        }
        val attestationGenerator = AttestationGenerator(validatorKeys, rnd)
        createAndImportBlockAtSlot(
                recentChainData.bestSlot.plus(UnsignedLong.valueOf(Constants.MIN_ATTESTATION_INCLUSION_DELAY.toLong())))
        while (recentChainData.store.finalizedCheckpoint.epoch.compareTo(epoch) < 0) {
            val headState = recentChainData
                    .store
                    .getBlockState(recentChainData.bestBlockRoot.orElseThrow())
            val headBlock = recentChainData.store.getBlock(recentChainData.bestBlockRoot.orElseThrow())
            val slot = recentChainData.bestSlot
            val currentSlotAssignments: SSZList<Attestation> = SSZList.createMutable(
                    attestationGenerator.getAttestationsForSlot(headState, headBlock, slot),
                    Constants.MAX_ATTESTATIONS.toLong(),
                    Attestation::class.java)
            createAndImportBlockAtSlot(
                    recentChainData.bestSlot.plus(UnsignedLong.ONE),
                    Optional.of(currentSlotAssignments),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty())
        }
    }

    fun getWrongProposerIndex(actualProposerIndex: Int): Int {
        return if (actualProposerIndex == 0) 1 else actualProposerIndex - 1
    }

    fun getSigner(proposerIndex: Int): MessageSignerService {
        return TestMessageSignerService(validatorKeys[proposerIndex])
    }

    fun initializeStorage(
            recentChainData: RecentChainData = this.recentChainData, validatorKeys: List<BLSKeyPair> = this.validatorKeys, signDeposits: Boolean = true) {
        StartupUtil.setupInitialState(recentChainData, 0, null, validatorKeys, signDeposits)
    }

    companion object {
        fun create(
                validatorCount: Int, storageClient: RecentChainData): BeaconChainUtil {
            val validatorKeys = MockStartValidatorKeyPairFactory().generateKeyPairs(0, validatorCount)
            return create(storageClient, validatorKeys)
        }

        fun create(
                storageClient: RecentChainData,
                validatorKeys: List<BLSKeyPair>,
                signDeposits: Boolean = true): BeaconChainUtil {
            return BeaconChainUtil(validatorKeys, storageClient, signDeposits)
        }

    }

    init {
        blockImporter = BlockImporter(recentChainData, EventBus())
    }
}