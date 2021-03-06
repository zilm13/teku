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

package tech.pegasys.teku.api.schema;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tech.pegasys.teku.datastructures.util.DataStructureUtil;

public class BeaconStateTest {
  private final DataStructureUtil dataStructureUtil = new DataStructureUtil();
  private final tech.pegasys.teku.datastructures.state.BeaconState beaconStateInternal =
      dataStructureUtil.randomBeaconState();

  @Test
  public void shouldConvertToInternalObject() {
    BeaconState beaconState = new BeaconState(beaconStateInternal);

    assertThat(beaconState.asInternalBeaconState()).isEqualTo(beaconStateInternal);
  }
}
