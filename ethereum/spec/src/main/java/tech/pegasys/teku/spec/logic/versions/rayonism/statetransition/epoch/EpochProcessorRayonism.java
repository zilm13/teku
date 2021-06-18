/*
 * Copyright 2021 ConsenSys AG.
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

package tech.pegasys.teku.spec.logic.versions.rayonism.statetransition.epoch;

import static tech.pegasys.teku.spec.datastructures.util.BeaconStateUtil.get_current_epoch;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Pair;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.config.SpecConfig;
import tech.pegasys.teku.spec.datastructures.state.Validator;
import tech.pegasys.teku.spec.datastructures.state.Withdrawal;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.BeaconState;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.MutableBeaconState;
import tech.pegasys.teku.spec.datastructures.state.beaconstate.versions.rayonism.MutableBeaconStateRayonism;
import tech.pegasys.teku.spec.logic.common.helpers.BeaconStateAccessors;
import tech.pegasys.teku.spec.logic.common.helpers.BeaconStateMutators;
import tech.pegasys.teku.spec.logic.common.helpers.MiscHelpers;
import tech.pegasys.teku.spec.logic.common.statetransition.epoch.AbstractEpochProcessor;
import tech.pegasys.teku.spec.logic.common.statetransition.epoch.RewardAndPenaltyDeltas;
import tech.pegasys.teku.spec.logic.common.statetransition.epoch.status.ValidatorStatusFactory;
import tech.pegasys.teku.spec.logic.common.statetransition.epoch.status.ValidatorStatuses;
import tech.pegasys.teku.spec.logic.common.statetransition.exceptions.EpochProcessingException;
import tech.pegasys.teku.spec.logic.common.util.BeaconStateUtil;
import tech.pegasys.teku.spec.logic.common.util.ValidatorsUtil;
import tech.pegasys.teku.spec.logic.versions.phase0.statetransition.epoch.RewardsAndPenaltiesCalculatorPhase0;
import tech.pegasys.teku.ssz.primitive.SszUInt64;

public class EpochProcessorRayonism extends AbstractEpochProcessor {

  public EpochProcessorRayonism(
      final SpecConfig specConfig,
      final MiscHelpers miscHelpers,
      final BeaconStateAccessors beaconStateAccessors,
      final BeaconStateMutators beaconStateMutators,
      final ValidatorsUtil validatorsUtil,
      final BeaconStateUtil beaconStateUtil,
      final ValidatorStatusFactory validatorStatusFactory) {
    super(
        specConfig,
        miscHelpers,
        beaconStateAccessors,
        beaconStateMutators,
        validatorsUtil,
        beaconStateUtil,
        validatorStatusFactory);
  }

  @Override
  protected void processEpoch(final BeaconState preState, final MutableBeaconState state)
      throws EpochProcessingException {
    super.processEpoch(preState, state);
    processWithdrawals(state);
  }

  @Override
  public RewardAndPenaltyDeltas getRewardAndPenaltyDeltas(
      BeaconState state, ValidatorStatuses validatorStatuses) {
    final RewardsAndPenaltiesCalculatorPhase0 calculator =
        new RewardsAndPenaltiesCalculatorPhase0(
            specConfig, state, validatorStatuses, miscHelpers, beaconStateAccessors);

    return calculator.getDeltas();
  }

  @Override
  public void processParticipationUpdates(MutableBeaconState genericState) {
    // Rotate current/previous epoch attestations
    final MutableBeaconStateRayonism state = MutableBeaconStateRayonism.required(genericState);
    state.getPrevious_epoch_attestations().setAll(state.getCurrent_epoch_attestations());
    state.getCurrent_epoch_attestations().clear();
  }

  @Override
  public void processSyncCommitteeUpdates(final MutableBeaconState state) {
    // Nothing to do
  }

  public void processWithdrawals(MutableBeaconState genericState) {
    final MutableBeaconStateRayonism state = MutableBeaconStateRayonism.required(genericState);
    UInt64 current_epoch = get_current_epoch(state);
    List<Pair<Integer, Validator>> withdrawal_validators =
        IntStream.range(0, state.getValidators().size())
            .mapToObj(i -> Pair.of(i, state.getValidators().get(i)))
            .filter(
                validatorPair ->
                    validatorPair
                            .getRight()
                            .getWithdrawable_epoch()
                            .isLessThanOrEqualTo(current_epoch)
                        && validatorPair
                            .getRight()
                            .getWithdrawn_epoch()
                            .isLessThan(validatorPair.getRight().getWithdrawable_epoch()))
            .collect(Collectors.toList());
    Map<Byte, List<Pair<Integer, Validator>>> validators_by_target =
        withdrawal_validators.stream()
            .collect(
                Collectors.groupingBy(
                    validatorPair -> validatorPair.getRight().getWithdrawal_credentials().get(0),
                    LinkedHashMap::new,
                    Collectors.toList()));

    // Only Eth1 withdrawals are currently supported
    List<Pair<Integer, Validator>> eth1_withdrawal_validators =
        validators_by_target.getOrDefault(
            specConfig.getEth1AddressWithdrawalPrefix().get(0), Collections.emptyList());
    for (Pair<Integer, Validator> validatorPair : eth1_withdrawal_validators) {
      state
          .getValidators()
          .set(
              validatorPair.getLeft(),
              validatorPair
                  .getRight()
                  .withWithdrawn_epoch(current_epoch)
                  .withEffective_balance(UInt64.ZERO));
      UInt64 balance = state.getBalances().get(validatorPair.getLeft()).get();
      state.getBalances().set(validatorPair.getLeft(), SszUInt64.of(UInt64.ZERO));
      state
          .getWithdrawals()
          .append(
              new Withdrawal(
                  UInt64.fromLongBits(validatorPair.getLeft()),
                  validatorPair.getRight().getWithdrawal_credentials(),
                  current_epoch,
                  balance));
    }
  }
}
