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

package tech.pegasys.teku.exec.eth1engine;

import java.util.Collections;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.api.schema.ExecutableData;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes20;

public interface Eth1EngineClient {

  SafeFuture<Response<ExecutableData>> eth2ProduceBlock(ProduceBlockRequest request);

  SafeFuture<Response<Boolean>> eth2InsertBlock(InsertBlockRequest request);

  Eth1EngineClient Stub =
      new Eth1EngineClient() {
        private final Bytes ZERO_LOGS_BLOOM = Bytes.wrap(new byte[256]);

        @Override
        public SafeFuture<Response<ExecutableData>> eth2ProduceBlock(ProduceBlockRequest request) {
          return SafeFuture.completedFuture(
              new Response<>(
                  new ExecutableData(
                      request.parentHash,
                      Bytes32.random(),
                      Bytes20.ZERO,
                      Bytes32.ZERO,
                      UInt64.ZERO,
                      UInt64.ZERO,
                      Bytes32.ZERO,
                      ZERO_LOGS_BLOOM,
                      UInt64.ZERO,
                      Collections.emptyList())));
        }

        @Override
        public SafeFuture<Response<Boolean>> eth2InsertBlock(InsertBlockRequest request) {
          return SafeFuture.completedFuture(new Response<>(true));
        }
      };
}
