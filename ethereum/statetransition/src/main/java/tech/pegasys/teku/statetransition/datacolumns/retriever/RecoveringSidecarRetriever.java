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

package tech.pegasys.teku.statetransition.datacolumns.retriever;

import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.kzg.KZG;
import tech.pegasys.teku.spec.datastructures.blobs.versions.eip7594.DataColumnSidecar;
import tech.pegasys.teku.spec.datastructures.blobs.versions.eip7594.MatrixEntry;
import tech.pegasys.teku.spec.datastructures.blocks.BeaconBlock;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlockHeader;
import tech.pegasys.teku.spec.datastructures.util.DataColumnSlotAndIdentifier;
import tech.pegasys.teku.spec.logic.versions.feature.eip7594.helpers.MiscHelpersEip7594;
import tech.pegasys.teku.spec.schemas.SchemaDefinitionsEip7594;
import tech.pegasys.teku.statetransition.datacolumns.CanonicalBlockResolver;
import tech.pegasys.teku.statetransition.datacolumns.db.DataColumnSidecarDbAccessor;

public class RecoveringSidecarRetriever implements DataColumnSidecarRetriever {
  private static final Logger LOG = LogManager.getLogger("das-nyota");

  private final DataColumnSidecarRetriever delegate;
  private final KZG kzg;
  private final MiscHelpersEip7594 specHelpers;
  private final SchemaDefinitionsEip7594 schemaDefinitions;
  private final CanonicalBlockResolver blockResolver;
  private final DataColumnSidecarDbAccessor sidecarDB;
  private final AsyncRunner asyncRunner;
  private final Duration recoverInitiationTimeout;
  private final int columnCount;
  private final int recoverColumnCount;

  private final Map<UInt64, RecoveryEntry> recoveryBySlot = new ConcurrentHashMap<>();

  public RecoveringSidecarRetriever(
      final DataColumnSidecarRetriever delegate,
      final KZG kzg,
      final MiscHelpersEip7594 specHelpers,
      final SchemaDefinitionsEip7594 schemaDefinitionsElectra,
      final CanonicalBlockResolver blockResolver,
      final DataColumnSidecarDbAccessor sidecarDB,
      final AsyncRunner asyncRunner,
      final Duration recoverInitiationTimeout,
      final int columnCount) {
    this.delegate = delegate;
    this.kzg = kzg;
    this.specHelpers = specHelpers;
    this.schemaDefinitions = schemaDefinitionsElectra;
    this.blockResolver = blockResolver;
    this.sidecarDB = sidecarDB;
    this.asyncRunner = asyncRunner;
    this.recoverInitiationTimeout = recoverInitiationTimeout;
    this.columnCount = columnCount;
    this.recoverColumnCount = columnCount / 2;
  }

  @Override
  public SafeFuture<DataColumnSidecar> retrieve(final DataColumnSlotAndIdentifier columnId) {
    final SafeFuture<DataColumnSidecar> promise = delegate.retrieve(columnId);
    // TODO we probably need a better heuristics to submit requests for recovery
    asyncRunner
        .runAfterDelay(
            () -> {
              if (!promise.isDone()) {
                maybeInitiateRecovery(columnId, promise);
              }
            },
            recoverInitiationTimeout)
        .ifExceptionGetsHereRaiseABug();
    return promise;
  }

  @VisibleForTesting
  void maybeInitiateRecovery(
      final DataColumnSlotAndIdentifier columnId, final SafeFuture<DataColumnSidecar> promise) {
    blockResolver
        .getBlockAtSlot(columnId.slot())
        .thenPeek(
            maybeBlock -> {
              if (!maybeBlock.map(b -> b.getRoot().equals(columnId.blockRoot())).orElse(false)) {
                LOG.info("[nyota] Recovery: CAN'T initiate recovery for " + columnId);
                promise.completeExceptionally(
                    new NotOnCanonicalChainException(columnId, maybeBlock));
              } else {
                final BeaconBlock block = maybeBlock.orElseThrow();
                LOG.info("[nyota] Recovery: initiating recovery for " + columnId);
                final RecoveryEntry recovery = addRecovery(columnId, block);
                recovery.addRequest(columnId.columnIndex(), promise);
              }
            })
        .ifExceptionGetsHereRaiseABug();
  }

  private synchronized RecoveryEntry addRecovery(
      final DataColumnSlotAndIdentifier columnId, final BeaconBlock block) {
    return recoveryBySlot.compute(
        columnId.slot(),
        (slot, existingRecovery) -> {
          if (existingRecovery != null
              && !existingRecovery.block.getRoot().equals(block.getRoot())) {
            // we are recovering obsolete column which is no more on our canonical chain
            existingRecovery.cancel();
          }
          if (existingRecovery == null || existingRecovery.cancelled) {
            return createNewRecovery(block);
          } else {
            return existingRecovery;
          }
        });
  }

  private RecoveryEntry createNewRecovery(final BeaconBlock block) {
    final RecoveryEntry recoveryEntry = new RecoveryEntry(block, kzg, specHelpers);
    LOG.info(
        "[nyota] Recovery: new RecoveryEntry for slot {} and block {} ",
        recoveryEntry.block.getSlot(),
        recoveryEntry.block.getRoot());
    sidecarDB
        .getColumnIdentifiers(block.getSlotAndBlockRoot())
        .thenCompose(
            dataColumnIdentifiers ->
                SafeFuture.collectAll(
                    dataColumnIdentifiers.stream()
                        .limit(recoverColumnCount)
                        .map(sidecarDB::getSidecar)))
        .thenPeek(
            maybeDataColumnSidecars -> {
              maybeDataColumnSidecars.forEach(
                  maybeDataColumnSidecar ->
                      maybeDataColumnSidecar.ifPresent(recoveryEntry::addSidecar));
              recoveryEntry.initRecoveryRequests();
            })
        .ifExceptionGetsHereRaiseABug();

    return recoveryEntry;
  }

  private synchronized void recoveryComplete(final RecoveryEntry entry) {
    LOG.trace("Recovery complete for entry {}", entry);
  }

  private class RecoveryEntry {
    private final BeaconBlock block;
    private final KZG kzg;
    private final MiscHelpersEip7594 specHelpers;

    private final Map<UInt64, DataColumnSidecar> existingSidecarsByColIdx = new HashMap<>();
    private final Map<UInt64, List<SafeFuture<DataColumnSidecar>>> promisesByColIdx =
        new HashMap<>();
    private List<SafeFuture<DataColumnSidecar>> recoveryRequests;
    private boolean recovered = false;
    private boolean cancelled = false;

    public RecoveryEntry(
        final BeaconBlock block, final KZG kzg, final MiscHelpersEip7594 specHelpers) {
      this.block = block;
      this.kzg = kzg;
      this.specHelpers = specHelpers;
    }

    public synchronized void addRequest(
        final UInt64 columnIndex, final SafeFuture<DataColumnSidecar> promise) {
      if (recovered) {
        promise.completeAsync(existingSidecarsByColIdx.get(columnIndex), asyncRunner);
      } else {
        promisesByColIdx.computeIfAbsent(columnIndex, __ -> new ArrayList<>()).add(promise);
        handleRequestCancel(columnIndex, promise);
      }
    }

    private void handleRequestCancel(
        final UInt64 columnIndex, final SafeFuture<DataColumnSidecar> request) {
      request.finish(
          __ -> {
            if (request.isCancelled()) {
              onRequestCancel(columnIndex);
            }
          });
    }

    private synchronized void onRequestCancel(final UInt64 columnIndex) {
      final List<SafeFuture<DataColumnSidecar>> promises = promisesByColIdx.remove(columnIndex);
      promises.stream().filter(p -> !p.isDone()).forEach(promise -> promise.cancel(true));
      if (promisesByColIdx.isEmpty()) {
        cancel();
      }
    }

    public synchronized void addSidecar(final DataColumnSidecar sidecar) {
      if (!recovered && sidecar.getBlockRoot().equals(block.getRoot())) {
        existingSidecarsByColIdx.put(sidecar.getIndex(), sidecar);
        if (existingSidecarsByColIdx.size() >= recoverColumnCount) {
          // TODO: Make it asynchronously as it's heavy CPU operation
          recover();
          recoveryComplete();
        }
      }
    }

    private void recoveryComplete() {
      recovered = true;
      LOG.info(
          "[nyota] Recovery: completed for the slot {}, requests complete: {}",
          block.getSlot(),
          promisesByColIdx.values().stream().mapToInt(List::size).sum());

      promisesByColIdx.forEach(
          (key, value) -> {
            DataColumnSidecar columnSidecar = existingSidecarsByColIdx.get(key);
            value.forEach(promise -> promise.completeAsync(columnSidecar, asyncRunner));
          });
      promisesByColIdx.clear();
      RecoveringSidecarRetriever.this.recoveryComplete(this);
      if (recoveryRequests != null) {
        recoveryRequests.forEach(r -> r.cancel(true));
        recoveryRequests = null;
      }
    }

    public synchronized void initRecoveryRequests() {
      if (!recovered && !cancelled) {
        recoveryRequests =
            IntStream.range(0, columnCount)
                .mapToObj(UInt64::valueOf)
                .filter(idx -> !existingSidecarsByColIdx.containsKey(idx))
                .map(
                    columnIdx ->
                        delegate.retrieve(
                            new DataColumnSlotAndIdentifier(
                                block.getSlot(), block.getRoot(), columnIdx)))
                .peek(
                    promise ->
                        promise
                            .thenPeek(this::addSidecar)
                            .ignoreCancelException()
                            .ifExceptionGetsHereRaiseABug())
                .toList();
      }
    }

    public synchronized void cancel() {
      cancelled = true;
      promisesByColIdx.values().stream()
          .flatMap(Collection::stream)
          .forEach(
              promise ->
                  asyncRunner.runAsync(() -> promise.cancel(true)).ifExceptionGetsHereRaiseABug());
      if (recoveryRequests != null) {
        recoveryRequests.forEach(rr -> rr.cancel(true));
      }
    }

    private void recover() {
      final List<List<MatrixEntry>> columnBlobEntries =
          existingSidecarsByColIdx.values().stream()
              .map(
                  sideCar ->
                      IntStream.range(0, sideCar.getDataColumn().size())
                          .mapToObj(
                              rowIndex ->
                                  schemaDefinitions
                                      .getMatrixEntrySchema()
                                      .create(
                                          sideCar.getDataColumn().get(rowIndex),
                                          sideCar.getSszKZGProofs().get(rowIndex).getKZGProof(),
                                          sideCar.getIndex(),
                                          UInt64.valueOf(rowIndex)))
                          .toList())
              .toList();
      final List<List<MatrixEntry>> blobColumnEntries = transpose(columnBlobEntries);
      final List<List<MatrixEntry>> extendedMatrix =
          specHelpers.recoverMatrix(blobColumnEntries, kzg);
      final DataColumnSidecar anyExistingSidecar =
          existingSidecarsByColIdx.values().stream().findFirst().orElseThrow();
      final SignedBeaconBlockHeader signedBeaconBlockHeader =
          anyExistingSidecar.getSignedBeaconBlockHeader();
      final List<DataColumnSidecar> recoveredSidecars =
          specHelpers.constructDataColumnSidecars(block, signedBeaconBlockHeader, extendedMatrix);
      final Map<UInt64, DataColumnSidecar> recoveredSidecarsAsMap =
          recoveredSidecars.stream()
              .collect(Collectors.toUnmodifiableMap(DataColumnSidecar::getIndex, i -> i));
      existingSidecarsByColIdx.putAll(recoveredSidecarsAsMap);
    }
  }

  private static <T> List<List<T>> transpose(final List<List<T>> matrix) {
    final int rowCount = matrix.size();
    final int colCount = matrix.get(0).size();
    final List<List<T>> ret =
        Stream.generate(() -> (List<T>) new ArrayList<T>(rowCount)).limit(colCount).toList();

    for (int row = 0; row < rowCount; row++) {
      if (matrix.get(row).size() != colCount) {
        throw new IllegalArgumentException("Different number columns in the matrix");
      }
      for (int col = 0; col < colCount; col++) {
        final T val = matrix.get(row).get(col);
        ret.get(col).add(row, val);
      }
    }
    return ret;
  }
}