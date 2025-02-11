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

package tech.pegasys.teku.statetransition.datacolumns.db;

import com.google.common.collect.Iterables;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.collections.LimitedMap;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.datastructures.blobs.versions.fulu.DataColumnSidecar;
import tech.pegasys.teku.spec.datastructures.util.DataColumnSlotAndIdentifier;

class ColumnIdCachingDasDb implements DataColumnSidecarDB {

  private final DataColumnSidecarDB delegateDb;
  private final Function<UInt64, Integer> slotToNumberOfColumns;

  private final Map<UInt64, SlotCache> slotCaches;

  public ColumnIdCachingDasDb(
      final DataColumnSidecarDB delegateDb,
      final Function<UInt64, Integer> slotToNumberOfColumns,
      final int maxCacheSize) {
    this.delegateDb = delegateDb;
    this.slotToNumberOfColumns = slotToNumberOfColumns;
    this.slotCaches = LimitedMap.createSynchronizedLRU(maxCacheSize);
  }

  private SlotCache getOrCreateSlotCache(final UInt64 slot) {
    return slotCaches.computeIfAbsent(
        slot,
        __ ->
            new SlotCache(
                delegateDb.getColumnIdentifiers(slot).thenApply(Function.identity()),
                slotToNumberOfColumns.apply(slot)));
  }

  @Override
  public SafeFuture<List<DataColumnSlotAndIdentifier>> getColumnIdentifiers(final UInt64 slot) {
    return getOrCreateSlotCache(slot).generateColumnIdentifiers(slot);
  }

  @Override
  public SafeFuture<Void> addSidecar(final DataColumnSidecar sidecar) {
    return getOrCreateSlotCache(sidecar.getSlot())
        .contains(sidecar.getDataColumnSlotAndIdentifier())
        .thenCompose(
            inDb -> {
              if (inDb) {
                return SafeFuture.COMPLETE;
              } else {
                return delegateDb
                    .addSidecar(sidecar)
                    .thenPeek(
                        __ -> {
                          final UInt64 slot = sidecar.getSlot();
                          updateSlotCache(slot, sidecar.getDataColumnSlotAndIdentifier());
                        });
              }
            });
  }

  private synchronized void updateSlotCache(
      final UInt64 slot, final DataColumnSlotAndIdentifier newIdentifier) {
    final SlotCache slotCache = getOrCreateSlotCache(slot);
    final Map<Bytes32, BitSet> oldSet = slotCache.compactCacheFuture.getImmediately();
    final List<DataColumnSlotAndIdentifier> columnIdentifiers =
        SlotCache.toColumnIdentifiers(slot, oldSet);
    final SlotCache updatedCache =
        new SlotCache(
            SafeFuture.completedFuture(Iterables.concat(columnIdentifiers, List.of(newIdentifier))),
            slotToNumberOfColumns.apply(slot));
    slotCaches.put(slot, updatedCache);
  }

  private static class SlotCache {
    private final SafeFuture<Map<Bytes32, BitSet>> compactCacheFuture;

    public SlotCache(
        final SafeFuture<Iterable<DataColumnSlotAndIdentifier>> dbResponseFuture,
        final int numberOfColumns) {
      this.compactCacheFuture =
          dbResponseFuture.thenApply(slotColumns -> toCompactCache(slotColumns, numberOfColumns));
    }

    public SafeFuture<List<DataColumnSlotAndIdentifier>> generateColumnIdentifiers(
        final UInt64 slot) {
      return compactCacheFuture.thenApply(compactCache -> toColumnIdentifiers(slot, compactCache));
    }

    public SafeFuture<Boolean> contains(final DataColumnSlotAndIdentifier id) {
      return compactCacheFuture.thenApply(
          cache -> toColumnIdentifiers(id.slot(), cache).contains(id));
    }

    private static Map<Bytes32, BitSet> toCompactCache(
        final Iterable<DataColumnSlotAndIdentifier> slotColumns, final int numberOfColumns) {
      final Map<Bytes32, BitSet> compactCache = new HashMap<>();
      slotColumns.forEach(
          colId ->
              compactCache
                  .computeIfAbsent(colId.blockRoot(), blockRoot -> new BitSet(numberOfColumns))
                  .set(colId.columnIndex().intValue()));
      return compactCache;
    }

    private static List<DataColumnSlotAndIdentifier> toColumnIdentifiers(
        final UInt64 slot, final Map<Bytes32, BitSet> compactCache) {
      return compactCache.entrySet().stream()
          .flatMap(
              entry ->
                  entry.getValue().stream()
                      .mapToObj(
                          colIndex ->
                              new DataColumnSlotAndIdentifier(
                                  slot, entry.getKey(), UInt64.valueOf(colIndex))))
          .toList();
    }
  }

  @Override
  public SafeFuture<Optional<UInt64>> getFirstCustodyIncompleteSlot() {
    return delegateDb.getFirstCustodyIncompleteSlot();
  }

  @Override
  public SafeFuture<Optional<UInt64>> getFirstSamplerIncompleteSlot() {
    return delegateDb.getFirstSamplerIncompleteSlot();
  }

  @Override
  public SafeFuture<Optional<DataColumnSidecar>> getSidecar(
      final DataColumnSlotAndIdentifier identifier) {
    return delegateDb.getSidecar(identifier);
  }

  @Override
  public SafeFuture<Void> setFirstCustodyIncompleteSlot(final UInt64 slot) {
    return delegateDb.setFirstCustodyIncompleteSlot(slot);
  }

  @Override
  public SafeFuture<Void> setFirstSamplerIncompleteSlot(final UInt64 slot) {
    return delegateDb.setFirstSamplerIncompleteSlot(slot);
  }

  @Override
  public SafeFuture<Void> pruneAllSidecars(final UInt64 tillSlot) {
    return delegateDb.pruneAllSidecars(tillSlot);
  }
}
