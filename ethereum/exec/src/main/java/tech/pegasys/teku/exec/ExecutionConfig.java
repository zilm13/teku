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

package tech.pegasys.teku.exec;

public class ExecutionConfig {
  private final String eth1EngineEndpoint;

  private ExecutionConfig(String eth1EngineEndpoint) {
    this.eth1EngineEndpoint = eth1EngineEndpoint;
  }

  public String getEth1EngineEndpoint() {
    return eth1EngineEndpoint;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String eth1EngineEndpoint;

    private Builder() {}

    public void eth1EngineEndpoint(String eth1EngineEndpoint) {
      this.eth1EngineEndpoint = eth1EngineEndpoint;
    }

    public ExecutionConfig build() {
      return new ExecutionConfig(eth1EngineEndpoint);
    }
  }
}
