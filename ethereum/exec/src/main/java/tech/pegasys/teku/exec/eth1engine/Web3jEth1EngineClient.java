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
import java.util.concurrent.CompletableFuture;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;
import tech.pegasys.teku.api.schema.ExecutableData;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class Web3jEth1EngineClient implements Eth1EngineClient {

  private final HttpService web3jService;

  public Web3jEth1EngineClient(String eth1Endpoint) {
    this.web3jService = new HttpService(eth1Endpoint);
  }

  @Override
  public SafeFuture<Response<ExecutableData>> eth2ProduceBlock(ProduceBlockRequest request) {
    Request<?, ProduceBlockWeb3jResponse> web3jRequest =
        new Request<>(
            "eth2_produceBlock",
            Collections.singletonList(request),
            web3jService,
            ProduceBlockWeb3jResponse.class);
    return doRequest(web3jRequest);
  }

  // Easter egg from my son:
  // fujffrfdsatfdtgfyghgghygyhyhyhhyhhyyuui8uuuyuyyyyuyuuyuuiu877776768ioppoop-00poliufffujk,
  // mjjhhhhhgyhgtggt66666666

  @Override
  public SafeFuture<Response<Boolean>> eth2InsertBlock(InsertBlockRequest request) {
    Request<?, InsertBlockWeb3jResponse> web3jRequest =
        new Request<>(
            "eth2_insertBlock",
            Collections.singletonList(request),
            web3jService,
            InsertBlockWeb3jResponse.class);
    return doRequest(web3jRequest);
  }

  private <T> SafeFuture<Response<T>> doRequest(
      Request<?, ? extends org.web3j.protocol.core.Response<T>> web3jRequest) {
    CompletableFuture<Response<T>> responseFuture =
        web3jRequest
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

  static class ProduceBlockWeb3jResponse extends org.web3j.protocol.core.Response<ExecutableData> {}

  static class InsertBlockWeb3jResponse extends org.web3j.protocol.core.Response<Boolean> {}
}
