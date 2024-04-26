/*
 * Copyright Consensys Software Inc., 2024
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

package tech.pegasys.teku.statetransition.datacolumns;

import java.util.Optional;
import java.util.stream.Stream;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.datastructures.blobs.versions.electra.DataColumnSidecar;
import tech.pegasys.teku.spec.datastructures.networking.libp2p.rpc.DataColumnIdentifier;
import tech.pegasys.teku.spec.datastructures.util.ColumnSlotAndIdentifier;
import tech.pegasys.teku.storage.api.SidecarUpdateChannel;
import tech.pegasys.teku.storage.client.CombinedChainDataClient;

// FIXME: remove stinky joins
public class DataColumnSidecarDBImpl implements DataColumnSidecarDB {
  private final CombinedChainDataClient combinedChainDataClient;
  private final SidecarUpdateChannel sidecarUpdateChannel;

  public DataColumnSidecarDBImpl(
      final CombinedChainDataClient combinedChainDataClient,
      final SidecarUpdateChannel sidecarUpdateChannel) {
    this.combinedChainDataClient = combinedChainDataClient;
    this.sidecarUpdateChannel = sidecarUpdateChannel;
  }

  @Override
  public Optional<UInt64> getFirstIncompleteSlot() {
    return combinedChainDataClient.getFirstIncompleteSlot().join();
  }

  @Override
  public Optional<DataColumnSidecar> getSidecar(final DataColumnIdentifier identifier) {
    return combinedChainDataClient.getSidecar(identifier).join();
  }

  @Override
  public Stream<DataColumnIdentifier> streamColumnIdentifiers(final UInt64 slot) {
    return combinedChainDataClient.getDataColumnIdentifiers(slot).join().stream()
        .map(ColumnSlotAndIdentifier::identifier);
  }

  @Override
  public void setFirstIncompleteSlot(final UInt64 slot) {
    sidecarUpdateChannel.onFirstIncompleteSlot(slot);
  }

  @Override
  public void addSidecar(final DataColumnSidecar sidecar) {
    sidecarUpdateChannel.onNewSidecar(sidecar);
  }

  @Override
  public void pruneAllSidecars(final UInt64 tillSlotExclusive) {
    sidecarUpdateChannel.onSidecarsAvailabilitySlot(tillSlotExclusive);
  }
}