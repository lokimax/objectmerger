package de.x132.objectmerger.registry;

import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;

/**
 * Registry for managing and retrieving merge strategies.
 *
 * <p>This class is responsible for loading strategies via Java SPI and providing access to them. It
 * helps decouple the strategy loading logic from the merge orchestration.
 */
@Slf4j
public class StrategyRegistry {

  private static final StrategyRegistry INSTANCE = new StrategyRegistry();
  private final Map<String, MergeStrategy<?>> strategies;

  private StrategyRegistry() {
    strategies = new HashMap<>();
    ServiceLoader<MergeStrategy> loader = ServiceLoader.load(MergeStrategy.class);
    for (MergeStrategy<?> strategy : loader) {
      strategies.put(strategy.getName(), strategy);
      log.debug("Loaded strategy: {}", strategy.getName());
    }
  }

  public static StrategyRegistry getInstance() {
    return INSTANCE;
  }

  /**
   * Retrieves a strategy by its name.
   *
   * @param name The name of the strategy.
   * @return The requested strategy, or the "standard" strategy if the name is not found.
   */
  public MergeStrategy<?> getStrategy(String name) {
    MergeStrategy<?> strategy = strategies.get(name);
    if (strategy == null) {
      log.warn("Unknown strategy '{}'. Using default 'standard'.", name);
      return strategies.get("standard");
    }
    return strategy;
  }
}
