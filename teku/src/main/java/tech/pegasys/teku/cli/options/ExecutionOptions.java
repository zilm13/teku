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

package tech.pegasys.teku.cli.options;

import picocli.CommandLine.Option;
import tech.pegasys.teku.config.TekuConfiguration;

public class ExecutionOptions {

  @Option(
      names = {"--eth1-engine"},
      paramLabel = "<NETWORK>",
      description = "URL for Eth1 engine.",
      arity = "1")
  private String eth1Engine = null;

  public void configure(final TekuConfiguration.Builder builder) {
    builder.execution(executionBuilder -> executionBuilder.eth1EngineEndpoint(eth1Engine));
  }
}
