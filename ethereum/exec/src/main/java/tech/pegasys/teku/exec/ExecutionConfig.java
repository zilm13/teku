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
