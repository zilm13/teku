package tech.pegasys.teku.networking.discovery

import org.apache.tuweni.bytes.Bytes
import java.net.InetSocketAddress

class DiscoveryEvent(val data: Bytes, val from: InetSocketAddress, val to: InetSocketAddress) {
}