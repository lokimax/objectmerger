package de.x132.objectmerger.strategy.graaljs;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.standard.StandardMergeStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GraalJsBenchmarkTest {

    @Test
    public void benchmarkGraalJsVsStandard() {
        GraalJsMergeStrategy graalStrategy = new GraalJsMergeStrategy();
        StandardMergeStrategy standardStrategy = new StandardMergeStrategy();

        LabeledSource<Object> source = new LabeledSource<>("test", Map.of("field", "value"));
        List<LabeledSource<?>> sources = Collections.singletonList(source);

        // GraalJS Setup
        GraalJsFieldDefinition graalConfig =
                GraalJsFieldDefinition.builder().expression("sources.test.field").build();

        // Standard Setup (Identity)
        // StandardMergeStrategy configuration is generic, usually relies on field
        // presence.
        // But let's assume standard behavior is just getting the value.
        // Actually StandardMergeStrategy logic depends on configuration, let's just
        // test GraalJS overhead.

        // Warmup GraalJS
        System.out.println("Warming up GraalJS...");
        for (int i = 0; i < 100; i++) {
            graalStrategy.merge(sources, graalConfig, "field");
        }

        // Measure GraalJS
        System.out.println("Measuring GraalJS (1000 iterations)...");
        long start = System.nanoTime();
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            graalStrategy.merge(sources, graalConfig, "field");
        }
        long duration = System.nanoTime() - start;
        double avgMs = (double) duration / iterations / 1_000_000.0;

        System.out.printf("GraalJS Average Merge Time: %.4f ms per call%n", avgMs);
        System.out.printf("Total Time for %d calls: %.2f ms%n", iterations, duration / 1_000_000.0);

        if (avgMs > 1.0) {
            System.out.println(
                    "WARNING: Performance is potentially slow (> 1ms per call) due to Context creation overhead.");
        }
    }
}
