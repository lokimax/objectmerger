package de.x132.objectmerger.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.x132.objectmerger.strategy.MergeStrategy;
import java.util.ServiceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StrategyRegistryTest {

    @Test
    @DisplayName("All registered strategies must return a non-null configuration class")
    void ensureStrictConfigurationClass() {
        StrategyRegistry registry = StrategyRegistry.getInstance();
        ServiceLoader<MergeStrategy> loader = ServiceLoader.load(MergeStrategy.class);
        for (MergeStrategy strategy : loader) {
            assertNotNull(
                    strategy.getConfigurationClass(),
                    "Strategy "
                            + strategy.getName()
                            + " must return a non-null configuration class");
        }
    }

    @Test
    @DisplayName("getStrategy returns the correct strategy for a valid name")
    void getStrategy_withValidName_returnsInstance() {
        StrategyRegistry registry = StrategyRegistry.getInstance();
        MergeStrategy<?, ?> strategy = registry.getStrategy("concatenate");
        assertNotNull(strategy, "Strategy 'concatenate' should be found");
        assertEquals("concatenate", strategy.getName());
    }

    @Test
    @DisplayName("getStrategy returns 'standard' strategy for unknown names")
    void getStrategy_withUnknownName_returnsStandard() {
        StrategyRegistry registry = StrategyRegistry.getInstance();
        MergeStrategy<?, ?> strategy = registry.getStrategy("non_existent_strategy_name");
        assertNotNull(strategy, "Should return a fallback strategy");
        assertEquals("standard", strategy.getName(), "Should fallback to 'standard'");
    }

    @Test
    @DisplayName("All loaded strategies have consistent names")
    void verifyNameConsistency() {
        ServiceLoader<MergeStrategy> loader = ServiceLoader.load(MergeStrategy.class);
        for (MergeStrategy strategy : loader) {
            assertNotNull(strategy.getName(), "Strategy name must not be null");
            assertFalse(strategy.getName().isEmpty(), "Strategy name must not be empty");

            // Verify that the registry can find it back by its own name
            StrategyRegistry registry = StrategyRegistry.getInstance();
            MergeStrategy<?, ?> retrieved = registry.getStrategy(strategy.getName());
            assertEquals(
                    strategy.getClass(),
                    retrieved.getClass(),
                    "Registry should return correct instance for name: " + strategy.getName());
        }
    }
}
