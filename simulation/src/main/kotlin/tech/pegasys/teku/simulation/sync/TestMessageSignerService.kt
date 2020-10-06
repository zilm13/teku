package tech.pegasys.teku.simulation.sync

import org.apache.tuweni.bytes.Bytes
import tech.pegasys.teku.bls.BLS
import tech.pegasys.teku.bls.BLSKeyPair
import tech.pegasys.teku.bls.BLSSignature
import tech.pegasys.teku.core.signatures.MessageSignerService
import tech.pegasys.teku.util.async.SafeFuture

class TestMessageSignerService(private val blsKeyPair: BLSKeyPair) : MessageSignerService {
    override fun signBlock(signingRoot: Bytes): SafeFuture<BLSSignature> {
        return sign(signingRoot)
    }

    override fun signAttestation(signingRoot: Bytes): SafeFuture<BLSSignature> {
        return sign(signingRoot)
    }

    override fun signAggregationSlot(signingRoot: Bytes): SafeFuture<BLSSignature> {
        return sign(signingRoot)
    }

    override fun signAggregateAndProof(signingRoot: Bytes): SafeFuture<BLSSignature> {
        return sign(signingRoot)
    }

    override fun signRandaoReveal(signingRoot: Bytes): SafeFuture<BLSSignature> {
        return sign(signingRoot)
    }

    override fun signVoluntaryExit(signingRoot: Bytes): SafeFuture<BLSSignature> {
        return sign(signingRoot)
    }

    private fun sign(signingRoot: Bytes): SafeFuture<BLSSignature> {
        return SafeFuture.completedFuture(BLS.sign(blsKeyPair.secretKey, signingRoot))
    }

}