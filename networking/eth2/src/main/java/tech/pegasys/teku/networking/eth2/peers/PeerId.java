/*
 * Copyright Consensys Software Inc., 2025
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

package tech.pegasys.teku.networking.eth2.peers;

import java.util.Objects;
import java.util.Optional;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.networking.p2p.peer.NodeId;

public final class PeerId {
  private final NodeId libp2pId;
  private final UInt256 discoveryId;

  private PeerId(final NodeId libp2pId, final UInt256 discoveryId) {
    this.libp2pId = libp2pId;
    this.discoveryId = discoveryId;
  }

  public static PeerId ofExisting(final NodeId libp2pId, final Optional<UInt256> discoveryId) {
    Objects.requireNonNull(libp2pId, "id must not be null");
    return new PeerId(libp2pId, discoveryId.orElse(null));
  }

  public static PeerId ofCandidate(final UInt256 id) {
    Objects.requireNonNull(id, "id must not be null");
    return new PeerId(null, id);
  }

  public boolean isExisting() {
    return libp2pId != null;
  }

  public boolean isCandidate() {
    return libp2pId == null;
  }

  public Optional<NodeId> getLibp2pId() {
    return Optional.ofNullable(libp2pId);
  }

  public Optional<UInt256> getDiscoveryId() {
    return Optional.ofNullable(discoveryId);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PeerId peerId)) {
      return false;
    }
    return Objects.equals(libp2pId, peerId.libp2pId)
        && Objects.equals(discoveryId, peerId.discoveryId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(libp2pId, discoveryId);
  }
}
