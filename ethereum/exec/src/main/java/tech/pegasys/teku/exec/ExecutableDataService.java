/*
 * Copyright 2020 ConsenSys AG.
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

package tech.pegasys.teku.exec;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.datastructures.blocks.exec.ExecutableData;
import tech.pegasys.teku.exec.eth1engine.Eth1EngineClient;
import tech.pegasys.teku.exec.eth1engine.InsertBlockRequest;
import tech.pegasys.teku.exec.eth1engine.ProduceBlockRequest;
import tech.pegasys.teku.exec.eth1engine.Response;
import tech.pegasys.teku.exec.eth1engine.Web3jEth1EngineClient;
import tech.pegasys.teku.infrastructure.logging.ColorConsolePrinter;
import tech.pegasys.teku.infrastructure.logging.ColorConsolePrinter.Color;
import tech.pegasys.teku.infrastructure.logging.LogFormatter;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class ExecutableDataService {

  private static final Logger LOG = LogManager.getLogger();

  public static final ExecutableDataService Stub = new ExecutableDataService(Eth1EngineClient.Stub);

  private final Eth1EngineClient eth1EngineClient;

  public static ExecutableDataService create(String eth1EngineEndpoint) {
    Eth1EngineClient eth1EngineClient =
        eth1EngineEndpoint != null
            ? new Web3jEth1EngineClient(eth1EngineEndpoint)
            : Eth1EngineClient.Stub;

    return new ExecutableDataService(eth1EngineClient);
  }

  ExecutableDataService(Eth1EngineClient eth1EngineClient) {
    this.eth1EngineClient = eth1EngineClient;
  }

  /**
   * Requests eth1-engine to produce a block.
   *
   * @param parentHash the hash of eth1 block to produce atop of
   * @param randaoMix the most recent randao mix
   * @param slot current slot that the proposal is happening in
   * @param timestamp the timestamp of the beginning of the slot
   * @param recentBeaconBlockRoots a list of recent beacon block roots
   * @return a response with executable data payload
   */
  public ExecutableData produce(
      Bytes32 parentHash,
      Bytes32 randaoMix,
      UInt64 slot,
      UInt64 timestamp,
      List<Bytes32> recentBeaconBlockRoots) {

    ProduceBlockRequest request =
        new ProduceBlockRequest(parentHash, randaoMix, slot, timestamp, recentBeaconBlockRoots);

    try {
      Response<tech.pegasys.teku.api.schema.ExecutableData> response =
          eth1EngineClient.eth2ProduceBlock(request).get();

      checkArgument(
          response.getPayload() != null,
          "Failed eth2_produceBlock(parent_hash=%s, randao_mix=%s, slot=%s, timestamp=%s, recent_beacon_block_roots=%s), reason: %s",
          LogFormatter.formatHashRoot(parentHash),
          LogFormatter.formatHashRoot(randaoMix),
          slot,
          timestamp,
          formatRoots(recentBeaconBlockRoots),
          response.getReason());

      LOG.info(
          ColorConsolePrinter.print(
              String.format(
                  "eth2_produceBlock(parent_hash=%s, randao_mix=%s, slot=%s, timestamp=%s, recent_beacon_block_roots=%s) ~> %s",
                  LogFormatter.formatHashRoot(parentHash),
                  LogFormatter.formatHashRoot(randaoMix),
                  slot,
                  timestamp,
                  formatRoots(recentBeaconBlockRoots),
                  response.getPayload()),
              Color.CYAN));

      return response.getPayload().asInternalExecutableData();
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Requests eth1-engine to insert a block.
   *
   * @param randaoMix the most recent randao mix
   * @param slot current slot
   * @param timestamp the timestamp of the beginning of the slot
   * @param recentBeaconBlockRoots a list of recent beacon block roots
   * @param executableData an executable payload
   * @return {@code true} if processing succeeded, {@code false} otherwise
   */
  public boolean insert(
      Bytes32 randaoMix,
      UInt64 slot,
      UInt64 timestamp,
      List<Bytes32> recentBeaconBlockRoots,
      ExecutableData executableData) {

    InsertBlockRequest request =
        new InsertBlockRequest(
            randaoMix,
            slot,
            timestamp,
            recentBeaconBlockRoots,
            new tech.pegasys.teku.api.schema.ExecutableData(executableData));

    try {
      Response<Boolean> response = eth1EngineClient.eth2InsertBlock(request).get();

      checkArgument(
          response.getPayload() != null,
          "Failed eth2_insertBlock(randao_mix=%s, slot=%s, timestamp=%s, recent_beacon_block_roots=%s, executable_data=%s), reason: %s",
          LogFormatter.formatHashRoot(randaoMix),
          slot,
          timestamp,
          formatRoots(recentBeaconBlockRoots),
          executableData,
          response.getReason());

      LOG.info(
          ColorConsolePrinter.print(
              String.format(
                  "eth2_insertBlock(randao_mix=%s, slot=%s, timestamp=%s, recent_beacon_block_roots=%s, executable_data=%s) ~> %s",
                  LogFormatter.formatHashRoot(randaoMix),
                  slot,
                  timestamp,
                  formatRoots(recentBeaconBlockRoots),
                  executableData,
                  response.getPayload()),
              Color.CYAN));

      return Boolean.TRUE.equals(response.getPayload());
    } catch (InterruptedException | ExecutionException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String formatRoots(List<Bytes32> roots) {
    return String.format(
        "[%s roots: %s .. %s]",
        roots.size(),
        tech.pegasys.teku.infrastructure.logging.LogFormatter.formatHashRoot(roots.get(0)),
        tech.pegasys.teku.infrastructure.logging.LogFormatter.formatHashRoot(
            roots.get(roots.size() - 1)));
  }
}
