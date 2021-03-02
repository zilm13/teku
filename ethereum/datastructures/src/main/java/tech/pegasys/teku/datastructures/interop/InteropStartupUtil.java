/*
 * Copyright 2019 ConsenSys AG.
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

package tech.pegasys.teku.datastructures.interop;

import java.util.List;
import java.util.stream.IntStream;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.datastructures.operations.DepositData;
import tech.pegasys.teku.datastructures.state.BeaconState;
import tech.pegasys.teku.datastructures.util.DepositGenerator;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public final class InteropStartupUtil {

  public static BeaconState createMockedStartInitialBeaconState(
      final long genesisTime, final int numValidators) {
    final List<BLSKeyPair> validatorKeys =
        new MockStartValidatorKeyPairFactory().generateKeyPairs(0, numValidators);
    return createMockedStartInitialBeaconState(genesisTime, validatorKeys, true);
  }

  public static BeaconState createMockedStartInitialBeaconState(
      final Bytes32 eth1BlockHash, final long genesisTime, List<BLSKeyPair> validatorKeys) {
    return createMockedStartInitialBeaconState(eth1BlockHash, genesisTime, validatorKeys, true);
  }

  public static BeaconState createMockedStartInitialBeaconState(
      final long genesisTime, List<BLSKeyPair> validatorKeys, boolean signDeposits) {
    return createMockedStartInitialBeaconState(
        MockStartBeaconStateGenerator.INTEROP_ETH1_BLOCK_HASH,
        genesisTime,
        validatorKeys,
        signDeposits);
  }

  public static BeaconState createMockedStartInitialBeaconState(
      final Bytes32 eth1BlockHash,
      final long genesisTime,
      List<BLSKeyPair> validatorKeys,
      boolean signDeposits) {
    final List<DepositData> initialDepositData =
        new MockStartDepositGenerator(new DepositGenerator(signDeposits))
            .createDeposits(validatorKeys);
    // TODO: outputs all validator's private data, remove when not needed
    IntStream.range(0, validatorKeys.size())
        .forEach(
            index -> {
              BLSKeyPair keyPair = validatorKeys.get(index);
              DepositData depositData = initialDepositData.get(index);
              StringBuilder builder = new StringBuilder();
              builder.append(keyPair.getPublicKey().toAbbreviatedString());
              builder.append(":");
              builder.append(keyPair.getPublicKey().toBytesCompressed().toHexString());
              builder.append(":");
              builder.append(keyPair.getSecretKey().toBytes().toHexString());
              builder.append(":");
              builder.append(depositData.getWithdrawal_credentials().toHexString());
              System.out.println(builder.toString());
            });
    return new MockStartBeaconStateGenerator()
        .createInitialBeaconState(eth1BlockHash, UInt64.valueOf(genesisTime), initialDepositData);
  }
}
