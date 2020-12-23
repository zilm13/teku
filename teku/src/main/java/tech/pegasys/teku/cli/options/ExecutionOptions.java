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
