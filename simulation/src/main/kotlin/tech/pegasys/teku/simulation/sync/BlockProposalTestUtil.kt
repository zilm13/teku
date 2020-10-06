package tech.pegasys.teku.simulation.sync

import com.google.common.primitives.UnsignedLong
import org.apache.tuweni.bytes.Bytes32
import org.apache.tuweni.crypto.Hash.sha2_256
import org.apache.tuweni.ssz.SSZ
import tech.pegasys.teku.core.BlockProposalUtil
import tech.pegasys.teku.core.StateTransition
import tech.pegasys.teku.core.StateTransitionException
import tech.pegasys.teku.core.exceptions.EpochProcessingException
import tech.pegasys.teku.core.exceptions.SlotProcessingException
import tech.pegasys.teku.core.signatures.MessageSignerService
import tech.pegasys.teku.core.signatures.Signer
import tech.pegasys.teku.datastructures.blocks.BeaconBlockBodyLists
import tech.pegasys.teku.datastructures.blocks.Eth1Data
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock
import tech.pegasys.teku.datastructures.blocks.SignedBlockAndState
import tech.pegasys.teku.datastructures.operations.Attestation
import tech.pegasys.teku.datastructures.operations.Deposit
import tech.pegasys.teku.datastructures.operations.ProposerSlashing
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit
import tech.pegasys.teku.datastructures.state.BeaconState
import tech.pegasys.teku.datastructures.util.BeaconStateUtil
import tech.pegasys.teku.ssz.SSZTypes.SSZList
import tech.pegasys.teku.util.config.Constants
import java.util.Optional

class BlockProposalTestUtil {
    private val blockProposalUtil: BlockProposalUtil
    private val stateTransition: StateTransition

    @Throws(StateTransitionException::class)
    fun createNewBlock(
            signer: MessageSignerService,
            newSlot: UnsignedLong,
            state: BeaconState,
            parentBlockSigningRoot: Bytes32,
            eth1Data: Eth1Data,
            attestations: SSZList<Attestation>,
            slashings: SSZList<ProposerSlashing>,
            deposits: SSZList<Deposit>,
            exits: SSZList<SignedVoluntaryExit>): SignedBlockAndState {
        val newEpoch = BeaconStateUtil.compute_epoch_at_slot(newSlot)
        val randaoReveal = Signer(signer).createRandaoReveal(newEpoch, state.forkInfo).join()
        val newBlockAndState = blockProposalUtil.createNewUnsignedBlock(
                newSlot,
                getProposerIndexForSlot(state, newSlot),
                randaoReveal,
                state,
                parentBlockSigningRoot,
                eth1Data,
                Bytes32.ZERO,
                attestations,
                slashings,
                BeaconBlockBodyLists.createAttesterSlashings(),
                deposits,
                exits)

        // Sign block and set block signature
        val block = newBlockAndState.block
        val blockSignature = Signer(signer).signBlock(block, state.forkInfo).join()
        val signedBlock = SignedBeaconBlock(block, blockSignature)
        return SignedBlockAndState(signedBlock, newBlockAndState.state)
    }

    @Throws(StateTransitionException::class)
    fun createBlock(
            signer: MessageSignerService,
            newSlot: UnsignedLong,
            previousState: BeaconState,
            parentBlockSigningRoot: Bytes32,
            attestations: Optional<SSZList<Attestation>>,
            deposits: Optional<SSZList<Deposit>>,
            exits: Optional<SSZList<SignedVoluntaryExit>>,
            eth1Data: Optional<Eth1Data>): SignedBlockAndState {
        val newEpoch = BeaconStateUtil.compute_epoch_at_slot(newSlot)
        return createNewBlock(
                signer,
                newSlot,
                previousState,
                parentBlockSigningRoot,
                eth1Data.orElse(get_eth1_data_stub(previousState, newEpoch)),
                attestations.orElse(BeaconBlockBodyLists.createAttestations()),
                BeaconBlockBodyLists.createProposerSlashings(),
                deposits.orElse(BeaconBlockBodyLists.createDeposits()),
                exits.orElse(BeaconBlockBodyLists.createVoluntaryExits()))
    }

    fun getProposerIndexForSlot(preState: BeaconState, slot: UnsignedLong): Int {
        val state: BeaconState
        state = try {
            stateTransition.process_slots(preState, slot)
        } catch (e: SlotProcessingException) {
            throw RuntimeException(e)
        } catch (e: EpochProcessingException) {
            throw RuntimeException(e)
        }
        return BeaconStateUtil.get_beacon_proposer_index(state)
    }

    companion object {
        private fun get_eth1_data_stub(state: BeaconState, current_epoch: UnsignedLong): Eth1Data {
            val epochs_per_period = UnsignedLong.valueOf(Constants.EPOCHS_PER_ETH1_VOTING_PERIOD.toLong())
            val voting_period = current_epoch.dividedBy(epochs_per_period)
            return Eth1Data(
                    sha2_256(SSZ.encodeUInt64(epochs_per_period.toLong())),
                    state.eth1_deposit_index,
                    sha2_256(sha2_256(SSZ.encodeUInt64(voting_period.toLong()))))
        }
    }

    init {
        stateTransition = StateTransition()
        blockProposalUtil = BlockProposalUtil(stateTransition)
    }
}