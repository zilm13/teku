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

package tech.pegasys.teku.beaconrestapi.handlers.v1.beacon;

import static tech.pegasys.teku.beaconrestapi.RestApiConstants.PARAM_STATE_ID;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.PARAM_STATE_ID_DESCRIPTION;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.PUBKEY_HASH;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.RES_BAD_REQUEST;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.RES_INTERNAL_ERROR;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.RES_NOT_FOUND;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.RES_OK;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.RES_SERVICE_UNAVAILABLE;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.SERVICE_UNAVAILABLE;
import static tech.pegasys.teku.beaconrestapi.RestApiConstants.TAG_V1_BEACON;

import com.google.common.base.Throwables;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiParam;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletResponse;
import org.apache.tuweni.bytes.Bytes32;
import org.jetbrains.annotations.NotNull;
import tech.pegasys.teku.api.ChainDataProvider;
import tech.pegasys.teku.api.DataProvider;
import tech.pegasys.teku.api.response.v1.beacon.GetWithdrawalResponse;
import tech.pegasys.teku.beaconrestapi.SingleQueryParameterUtils;
import tech.pegasys.teku.beaconrestapi.handlers.AbstractHandler;
import tech.pegasys.teku.beaconrestapi.schema.BadRequest;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.provider.JsonProvider;

public class GetWithdrawal extends AbstractHandler {
  public static final String ROUTE = "/eth/v1/beacon/states/:state_id/withdrawal";

  private final ChainDataProvider chainDataProvider;

  public GetWithdrawal(final DataProvider dataProvider, final JsonProvider jsonProvider) {
    this(dataProvider.getChainDataProvider(), jsonProvider);
  }

  GetWithdrawal(final ChainDataProvider provider, final JsonProvider jsonProvider) {
    super(jsonProvider);
    this.chainDataProvider = provider;
  }

  @OpenApi(
      path = ROUTE,
      method = HttpMethod.GET,
      summary = "Get withdrawal with proof from state",
      tags = {TAG_V1_BEACON},
      description =
          "Returns withdrawal and generalized state proof tree for validator withdrawal by hash of its public key",
      pathParams = {
        @OpenApiParam(name = PARAM_STATE_ID, description = PARAM_STATE_ID_DESCRIPTION),
      },
      queryParams = {
        @OpenApiParam(
            name = PUBKEY_HASH,
            description = "Validator's public key hash",
            isRepeatable = true)
      },
      responses = {
        @OpenApiResponse(
            status = RES_OK,
            content = @OpenApiContent(from = GetWithdrawalResponse.class)),
        @OpenApiResponse(status = RES_BAD_REQUEST),
        @OpenApiResponse(status = RES_INTERNAL_ERROR),
        @OpenApiResponse(status = RES_NOT_FOUND),
        @OpenApiResponse(status = RES_SERVICE_UNAVAILABLE, description = SERVICE_UNAVAILABLE)
      })
  @Override
  public void handle(@NotNull final Context ctx) throws Exception {
    final Map<String, List<String>> queryParamMap = ctx.queryParamMap();
    final Map<String, String> pathParamMap = ctx.pathParamMap();

    if (!queryParamMap.containsKey(PUBKEY_HASH)) {
      ctx.status(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    try {
      final Bytes32 pubkeyHash =
          SingleQueryParameterUtils.getParameterValueAsBytes32(queryParamMap, PUBKEY_HASH);
      ctx.result(
          chainDataProvider
              .getWithdrawalWithProof(pathParamMap.getOrDefault(PARAM_STATE_ID, "head"), pubkeyHash)
              .thenApplyChecked(
                  result -> jsonProvider.objectToJSON(new GetWithdrawalResponse(result.get())))
              .exceptionallyCompose(error -> handleError(ctx, error)));
    } catch (final IllegalArgumentException e) {
      ctx.status(HttpServletResponse.SC_BAD_REQUEST);
      ctx.result(jsonProvider.objectToJSON(new BadRequest(e.getMessage())));
    } catch (final NoSuchElementException e) {
      ctx.status(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private SafeFuture<String> handleError(final Context ctx, final Throwable error) {
    final Throwable rootCause = Throwables.getRootCause(error);
    if (rootCause instanceof IllegalArgumentException) {
      ctx.status(HttpServletResponse.SC_BAD_REQUEST);
      return SafeFuture.of(() -> BadRequest.badRequest(jsonProvider, error.getMessage()));
    }
    return SafeFuture.failedFuture(error);
  }
}
