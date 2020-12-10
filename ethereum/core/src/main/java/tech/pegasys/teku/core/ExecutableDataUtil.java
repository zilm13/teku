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

package tech.pegasys.teku.core;

import static tech.pegasys.teku.core.ForkChoiceUtil.getSlotStartTime;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_current_epoch;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_randao_mix;

import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.core.exceptions.BlockProcessingException;
import tech.pegasys.teku.datastructures.blocks.BeaconBlockBody;
import tech.pegasys.teku.datastructures.state.MutableBeaconState;
import tech.pegasys.teku.exec.eth1engine.Eth1EngineClient;
import tech.pegasys.teku.exec.eth1engine.Eth1EngineClient.Response;
import tech.pegasys.teku.exec.eth1engine.schema.ExecutableDTO;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public final class ExecutableDataUtil {

  private static final Logger LOG = LogManager.getLogger();

  private final Eth1EngineClient eth1EngineClient;

  public ExecutableDataUtil(Eth1EngineClient eth1EngineClient) {
    this.eth1EngineClient = eth1EngineClient;
  }

  public void process_executable_data(MutableBeaconState state, BeaconBlockBody body)
      throws BlockProcessingException {
    UInt64 slot = state.getSlot();
    UInt64 timestamp = getSlotStartTime(slot, state.getGenesis_time());
    UInt64 epoch = get_current_epoch(state);

    Bytes32 parent_hash = state.getLatest_block_header().getEth1ParentHash();
    // assumes randao mix of the beacon block has been processed
    Bytes32 randao_mix = get_randao_mix(state, epoch);
    ExecutableDTO executableDTO =
        Eth1EngineApiSchemaUtil.getExecutableDTO(body.getExecutable_data());

    try {
      Response<Boolean> response =
          eth1EngineClient
              .eth2InsertBlock(parent_hash, randao_mix, slot, timestamp, executableDTO)
              .get();

      if (!Boolean.TRUE.equals(response.getPayload())) {
        throw new IllegalStateException(
            "Failed to eth2_insertBlock("
                + "parent_hash="
                + parent_hash
                + ", randao_mix="
                + randao_mix
                + ", slot="
                + slot
                + ", timestamp="
                + timestamp
                + ", executable_data="
                + body.getExecutable_data()
                + "), reason: "
                + String.valueOf(response.getReason()));
      }

    } catch (IllegalArgumentException
        | IllegalStateException
        | InterruptedException
        | ExecutionException e) {
      LOG.warn(e.getMessage());
      throw new BlockProcessingException(e);
    }
  }
}
