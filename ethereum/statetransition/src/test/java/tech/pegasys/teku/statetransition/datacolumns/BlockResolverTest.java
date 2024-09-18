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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.datastructures.blocks.BeaconBlock;

public class BlockResolverTest {
  final List<UInt64> blockResolverEmptyVerifier = new ArrayList<>();
  private final BlockResolver blockResolverEmpty =
      slot -> {
        blockResolverEmptyVerifier.add(slot);
        return SafeFuture.completedFuture(Optional.empty());
      };

  private final BeaconBlock beaconBlock = mock(BeaconBlock.class);
  final List<UInt64> blockResolverWithBlockVerifier = new ArrayList<>();
  private final BlockResolver blockResolverWithBlock =
      slot -> {
        blockResolverWithBlockVerifier.add(slot);
        return SafeFuture.completedFuture(Optional.of(beaconBlock));
      };

  @BeforeEach
  public void setup() {
    blockResolverEmptyVerifier.clear();
    blockResolverWithBlockVerifier.clear();
  }

  @Test
  public void orElseShouldNotCheck2ndIf1stAvailable() {
    final BlockResolver testedBlockResolver = blockResolverWithBlock.orElse(blockResolverEmpty);

    assertThat(testedBlockResolver.getBlockAtSlot(UInt64.ONE))
        .isCompletedWithValue(Optional.of(beaconBlock));
    assertThat(blockResolverEmptyVerifier).isEmpty();
    assertThat(blockResolverWithBlockVerifier).containsExactly(UInt64.ONE);
  }

  @Test
  public void orElseShouldCheck2ndIf1stNotAvailable() {
    final BlockResolver testedBlockResolver =
        blockResolverEmpty.orElse(blockResolverEmpty).orElse(blockResolverWithBlock);

    assertThat(testedBlockResolver.getBlockAtSlot(UInt64.ONE))
        .isCompletedWithValue(Optional.of(beaconBlock));
    assertThat(blockResolverEmptyVerifier).containsExactly(UInt64.ONE, UInt64.ONE);
    assertThat(blockResolverWithBlockVerifier).containsExactly(UInt64.ONE);
  }

  @Test
  public void orElseShouldReturnEmptyIfAllNotAvailable() {
    final BlockResolver testedBlockResolver = blockResolverEmpty.orElse(blockResolverEmpty);

    assertThat(testedBlockResolver.getBlockAtSlot(UInt64.ONE))
        .isCompletedWithValue(Optional.empty());
    assertThat(blockResolverEmptyVerifier).containsExactly(UInt64.ONE, UInt64.ONE);
    assertThat(blockResolverWithBlockVerifier).isEmpty();
  }
}
