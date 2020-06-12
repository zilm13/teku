package tech.pegasys.teku.phase1.datastructures.config;

public interface ConstantsPhase1 {
  long MAX_SHARDS = 64;
  long MAX_EARLY_DERIVED_SECRET_REVEALS = 1;
  long EARLY_DERIVED_SECRET_PENALTY_MAX_FUTURE_EPOCHS = 1L << 14;
}
