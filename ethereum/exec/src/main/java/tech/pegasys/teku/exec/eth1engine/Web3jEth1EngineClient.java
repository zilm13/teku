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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;
import tech.pegasys.teku.exec.eth1engine.schema.ExecutableDataDTO;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class Web3jEth1EngineClient implements Eth1EngineClient {

  private final CustomJsonRpc2_0Web3j web3j;

  public static Web3jEth1EngineClient create(String eth1Endpoint) {
    final HttpService web3jService = new HttpService(eth1Endpoint);
    final CustomJsonRpc2_0Web3j web3j = new CustomJsonRpc2_0Web3j(web3jService);

    return new Web3jEth1EngineClient(web3j);
  }

  Web3jEth1EngineClient(CustomJsonRpc2_0Web3j web3j) {
    this.web3j = web3j;
  }

  @Override
  public Response<Bytes32> eth_getHeadBlockHash() {
    try {
      EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
      EthBlock block =
          web3j
              .ethGetBlockByNumber(
                  DefaultBlockParameter.valueOf(blockNumber.getBlockNumber()), false)
              .send();
      return new Response<>(Bytes32.fromHexString(block.getBlock().getHash()));
    } catch (Exception e) {
      return new Response<>(e.getMessage());
    }
  }

  @Override
  public SafeFuture<Response<ExecutableDataDTO>> eth2ProduceBlock(
      Bytes32 parentHash,
      Bytes32 randaoMix,
      UInt64 slot,
      UInt64 timestamp,
      List<Bytes32> recentBeaconBlockRoots) {
    Request<?, ProduceBlockResponse> request =
        web3j.eth2ProduceBlock(parentHash, randaoMix, slot, timestamp, recentBeaconBlockRoots);
    return processRequest(request);
  }

  @Override
  public SafeFuture<Response<Boolean>> eth2InsertBlock(
      Bytes32 parentHash,
      Bytes32 randaoMix,
      UInt64 slot,
      UInt64 timestamp,
      List<Bytes32> recentBeaconBlockRoots,
      ExecutableDataDTO executableData) {
    Request<?, InsertBlockResponse> request =
        web3j.eth2InsertBlock(
            parentHash, randaoMix, slot, timestamp, recentBeaconBlockRoots, executableData);
    return processRequest(request);
  }

  private <T> SafeFuture<Response<T>> processRequest(
      Request<?, ? extends org.web3j.protocol.core.Response<T>> request) {
    CompletableFuture<Response<T>> responseFuture =
        request
            .sendAsync()
            .handle(
                (response, exception) -> {
                  if (exception != null) {
                    return new Response<>(exception.getMessage());
                  } else if (response.hasError()) {
                    return new Response<>(
                        response.getError().getCode() + ": " + response.getError().getMessage());
                  } else {
                    return new Response<>(response.getResult());
                  }
                });
    return SafeFuture.of(responseFuture);
  }

  private static class CustomJsonRpc2_0Web3j extends JsonRpc2_0Web3j {

    public CustomJsonRpc2_0Web3j(Web3jService web3jService) {
      super(web3jService);
    }

    public Request<?, ProduceBlockResponse> eth2ProduceBlock(
        Bytes32 parentHash,
        Bytes32 randaoMix,
        UInt64 slot,
        UInt64 timestamp,
        List<Bytes32> recentBeaconBlockRoots) {
      Map<String, Object> params = new HashMap<>();
      params.put("parent_hash", parentHash.toHexString());
      params.put("randao_mix", randaoMix.toHexString());
      params.put("slot", slot.toString());
      params.put("timestamp", timestamp.toString());
      params.put(
          "recent_beacon_block_roots",
          recentBeaconBlockRoots.stream().map(Bytes::toHexString).collect(Collectors.toList()));

      return new Request<>(
          "eth2_produceBlock",
          Collections.singletonList(params),
          web3jService,
          ProduceBlockResponse.class);
    }

    public Request<?, InsertBlockResponse> eth2InsertBlock(
        Bytes32 parentHash,
        Bytes32 randaoMix,
        UInt64 slot,
        UInt64 timestamp,
        List<Bytes32> recentBeaconBlockRoots,
        ExecutableDataDTO executableData) {
      Map<String, Object> params = new HashMap<>();
      params.put("parent_hash", parentHash.toHexString());
      params.put("randao_mix", randaoMix.toHexString());
      params.put("slot", slot.toString());
      params.put("timestamp", timestamp.toString());
      params.put(
          "recent_beacon_block_roots",
          recentBeaconBlockRoots.stream().map(Bytes::toHexString).collect(Collectors.toList()));
      params.put("executable_data", executableData);

      return new Request<>(
          "eth2_insertBlock",
          Collections.singletonList(params),
          web3jService,
          InsertBlockResponse.class);
    }
  }

  private static class ProduceBlockResponse
      extends org.web3j.protocol.core.Response<ExecutableDataDTO> {}

  private static class InsertBlockResponse extends org.web3j.protocol.core.Response<Boolean> {}
}
