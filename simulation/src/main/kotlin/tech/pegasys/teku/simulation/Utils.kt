package tech.pegasys.teku.simulation

import io.libp2p.core.crypto.KEY_TYPE
import io.libp2p.core.crypto.generateKeyPair
import tech.pegasys.teku.networking.p2p.connection.TargetPeerRange
import tech.pegasys.teku.networking.p2p.network.GossipConfig
import tech.pegasys.teku.networking.p2p.network.NetworkConfig
import tech.pegasys.teku.networking.p2p.network.WireLogsConfig
import java.util.Optional
import java.util.OptionalInt

const val PORTS_PER_ADDRESS = 65535

fun genNetworkConfig(index: Int) : NetworkConfig {
    val pk = generateKeyPair(KEY_TYPE.SECP256K1).component1()
    val port = index % PORTS_PER_ADDRESS
    val ipMinor = index / PORTS_PER_ADDRESS + 1
    return NetworkConfig(
            pk,
            "127.0.0.$ipMinor",
            Optional.empty(),
            port,
            OptionalInt.empty(),
            emptyList(),
            false,
            emptyList(),
            TargetPeerRange(20, 30),
            GossipConfig.DEFAULT_CONFIG,
            WireLogsConfig(false, false, true, false))
}