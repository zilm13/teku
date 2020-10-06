package tech.pegasys.teku.simulation

import kotlinx.coroutines.CoroutineScope
import tech.pegasys.teku.networking.discovery.DiscoveryEvent
import java.net.InetSocketAddress

class Router() {
    private val discoveryMap = HashMap<InetSocketAddress, VirtualPeer>()

    fun register(peer: VirtualPeer) {
        discoveryMap[peer.networkConfig.address] = peer
    }

    suspend fun tick(event: DiscoveryEvent, scope: CoroutineScope) {
        discoveryMap[event.to]?.listenOnDiscovery(event, scope)
    }
}