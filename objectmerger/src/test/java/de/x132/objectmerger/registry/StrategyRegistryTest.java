package de.x132.objectmerger.registry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.x132.objectmerger.strategy.MergeStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StrategyRegistryTest {

  @Test
  @DisplayName("All registered strategies must return a non-null configuration class")
  void ensureStrictConfigurationClass() {
    StrategyRegistry registry = StrategyRegistry.getInstance();
    // Accessing private map via reflection or just iterating known strategies if
    // possible?
    // Using SPI load again for testing is safer and cleaner.
    java.util.ServiceLoader<MergeStrategy> loader =
        java.util.ServiceLoader.load(MergeStrategy.class);
    for (MergeStrategy strategy : loader) {
      assertNotNull(
          strategy.getConfigurationClass(),
          "Strategy " + strategy.getName() + " must return a non-null configuration class");
    }
  }
}
