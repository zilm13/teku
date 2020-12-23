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

import static com.google.common.base.Preconditions.checkArgument;
import static tech.pegasys.teku.core.ForkChoiceUtil.getSlotStartTime;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_block_root_at_slot;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_current_epoch;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.get_randao_mix;
import static tech.pegasys.teku.util.config.Constants.GENESIS_SLOT;
import static tech.pegasys.teku.util.config.Constants.BLOCK_ROOTS_FOR_EVM_SIZE;
import static tech.pegasys.teku.util.config.Constants.SLOTS_PER_HISTORICAL_ROOT;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.core.exceptions.BlockProcessingException;
import tech.pegasys.teku.datastructures.blocks.BeaconBlockBody;
import tech.pegasys.teku.datastructures.state.BeaconState;
import tech.pegasys.teku.datastructures.state.MutableBeaconState;
import tech.pegasys.teku.exec.ExecutableDataService;
import tech.pegasys.teku.infrastructure.logging.LogFormatter;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public final class ExecutableDataUtil {

  private static final Logger LOG = LogManager.getLogger();

  private final ExecutableDataService executableDataService;

  public ExecutableDataUtil(ExecutableDataService executableDataService) {
    this.executableDataService = executableDataService;
  }

  public void process_executable_data(MutableBeaconState state, BeaconBlockBody body)
      throws BlockProcessingException {
    UInt64 slot = state.getSlot();
    UInt64 timestamp = getSlotStartTime(slot, state.getGenesis_time());
    UInt64 epoch = get_current_epoch(state);

    // assumes randao mix of the beacon block has been processed
    Bytes32 randao_mix = get_randao_mix(state, epoch);
    List<Bytes32> recent_block_roots = get_block_roots_for_evm(state);

    try {
      boolean response =
          executableDataService.insert(
              randao_mix, slot, timestamp, recent_block_roots, body.getExecutable_data());

      checkArgument(
          response,
          "process_executable_data failed: randao_mix=%s, slot=%s, timestamp=%s, executable_data=%s",
          LogFormatter.formatHashRoot(randao_mix),
          slot,
          timestamp,
          body.getExecutable_data());

    } catch (IllegalArgumentException | IllegalStateException e) {
      LOG.warn(e.getMessage());
      throw new BlockProcessingException(e);
    }
  }

  public static List<Bytes32> get_block_roots_for_evm(BeaconState state) {
    return get_recent_block_roots(
        state, Math.min(BLOCK_ROOTS_FOR_EVM_SIZE, SLOTS_PER_HISTORICAL_ROOT));
  }

  public static List<Bytes32> get_recent_block_roots(BeaconState state, long qty) {
    UInt64 current_slot = state.getSlot();
    List<Bytes32> result =
        LongStream.rangeClosed(1, qty)
            .mapToObj(
                idx -> {
                  if (current_slot.minus(GENESIS_SLOT).isLessThan(UInt64.valueOf(idx))) {
                    return Bytes32.ZERO;
                  } else {
                    return get_block_root_at_slot(state, current_slot.minus(idx));
                  }
                })
            .collect(Collectors.toList());

    // reverting to ascending order
    Collections.reverse(result);
    return result;
  }
}
