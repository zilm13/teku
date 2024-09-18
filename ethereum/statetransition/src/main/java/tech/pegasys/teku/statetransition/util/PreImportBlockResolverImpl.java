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

package tech.pegasys.teku.statetransition.util;

import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeMap;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.datastructures.blocks.BeaconBlock;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.statetransition.datacolumns.PreImportBlockResolver;

public class PreImportBlockResolverImpl extends AbstractIgnoringFutureHistoricalSlot
    implements PreImportBlockResolver {
  final NavigableMap<UInt64, BeaconBlock> beaconBlocks;
  final int maxBlocks;

  public PreImportBlockResolverImpl(
      final Spec spec,
      final UInt64 historicalSlotTolerance,
      final UInt64 futureSlotTolerance,
      final int maxBlocks) {
    super(spec, historicalSlotTolerance, futureSlotTolerance);
    this.maxBlocks = maxBlocks;
    this.beaconBlocks = new TreeMap<>();
  }

  @Override
  public synchronized void onNewBlock(final SignedBeaconBlock block) {
    makeRoomForNewBlocks();
    beaconBlocks.put(block.getSlot(), block.getMessage());
  }

  @Override
  public synchronized void removeBlockAtSlot(final UInt64 slot) {
    beaconBlocks.remove(slot);
  }

  void makeRoomForNewBlocks() {
    while (beaconBlocks.size() > maxBlocks) {
      UInt64 first = beaconBlocks.navigableKeySet().first();
      removeBlockAtSlot(first);
    }
  }

  @Override
  synchronized void prune(final UInt64 slotLimit) {
    final NavigableSet<UInt64> slotsToPrune =
        beaconBlocks.headMap(slotLimit, true).navigableKeySet();
    slotsToPrune.forEach(beaconBlocks::remove);
  }

  @Override
  public synchronized SafeFuture<Optional<BeaconBlock>> getBlockAtSlot(final UInt64 slot) {
    return SafeFuture.completedFuture(Optional.ofNullable(beaconBlocks.get(slot)));
  }
}
