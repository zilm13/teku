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

import java.math.BigInteger;
import java.util.Collections;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.exec.eth1engine.schema.ExecutableDTO;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.Bytes20;

public interface Eth1EngineClient {

  /**
   * Returns the hash of the head of the chain.
   *
   * <p>Used as a workaround to build on top of genesis block.
   *
   * @return a hash of the head block
   */
  Response<Bytes32> eth_getHeadBlockHash();

  /**
   * Requests eth1-engine to produce a block.
   *
   * @param parentHash the hash of eth1 block to produce atop of
   * @param randaoMix the most recent randao mix
   * @param slot current slot that the proposal is happening in
   * @param timestamp the timestamp of the beginning of the slot
   * @return a response with executable data payload
   */
  SafeFuture<Response<ExecutableDTO>> eth2ProduceBlock(
      Bytes32 parentHash, Bytes32 randaoMix, UInt64 slot, UInt64 timestamp);

  /**
   * Requests eth1-engine to insert a block.
   *
   * @param parentHash the hash of the parent eth1 block
   * @param randaoMix the most recent randao mix
   * @param slot current slot
   * @param timestamp the timestamp of the beginning of the slot
   * @param executableData an executable payload
   * @return {@code true} if processing succeeded, {@code false} otherwise
   */
  SafeFuture<Response<Boolean>> eth2InsertBlock(
      Bytes32 parentHash,
      Bytes32 randaoMix,
      UInt64 slot,
      UInt64 timestamp,
      ExecutableDTO executableData);

  static Eth1EngineClient createWeb3jClient(String eth1Endpoint) {
    return Web3jEth1EngineClient.create(eth1Endpoint);
  }

  final class Response<T> {
    private final T payload;
    private final String reason;

    private Response(T payload, String reason) {
      this.payload = payload;
      this.reason = reason;
    }

    public Response(String reason) {
      this(null, reason);
    }

    public Response(T payload) {
      this.payload = payload;
      this.reason = null;
    }

    public T getPayload() {
      return payload;
    }

    public String getReason() {
      return reason;
    }
  }

  Eth1EngineClient Stub =
      new Eth1EngineClient() {
        @Override
        public Response<Bytes32> eth_getHeadBlockHash() {
          return new Response<>(Bytes32.ZERO);
        }

        @Override
        public SafeFuture<Response<ExecutableDTO>> eth2ProduceBlock(
            Bytes32 parentHash, Bytes32 randaoMix, UInt64 slot, UInt64 timestamp) {
          return SafeFuture.completedFuture(
              new Response<>(
                  new ExecutableDTO(
                      Bytes32.ZERO.toHexString(),
                      Bytes20.ZERO.toHexString(),
                      Bytes32.ZERO.toHexString(),
                      0L,
                      0L,
                      Bytes32.ZERO.toHexString(),
                      Bytes.wrap(new byte[256]).toHexString(),
                      0L,
                      BigInteger.ZERO,
                      Collections.emptyList())));
        }

        @Override
        public SafeFuture<Response<Boolean>> eth2InsertBlock(
            Bytes32 parentHash,
            Bytes32 randaoMix,
            UInt64 slot,
            UInt64 timestamp,
            ExecutableDTO executableData) {
          return SafeFuture.completedFuture(new Response<>(true));
        }
      };
}
