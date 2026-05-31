package de.x132.objectmerger.strategy.nashorn;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.standard.StandardMergeStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NashornBenchmarkTest {

    @Test
    public void benchmarkNashornVsStandard() {
        NashornMergeStrategy nashornStrategy = new NashornMergeStrategy();
        StandardMergeStrategy standardStrategy = new StandardMergeStrategy();

        LabeledSource<Object> source = new LabeledSource<>("test", Map.of("field", "value"));
        List<LabeledSource<?>> sources = Collections.singletonList(source);

        NashornFieldDefinition nashornConfig =
                NashornFieldDefinition.builder().expression("sources.test.field").build();

        // Warmup Nashorn
        System.out.println("Warming up Nashorn...");
        for (int i = 0; i < 100; i++) {
            nashornStrategy.merge(sources, nashornConfig, "field");
        }

        // Measure Nashorn
        System.out.println("Measuring Nashorn (1000 iterations)...");
        long start = System.nanoTime();
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            nashornStrategy.merge(sources, nashornConfig, "field");
        }
        long duration = System.nanoTime() - start;
        double avgMs = (double) duration / iterations / 1_000_000.0;

        System.out.printf("Nashorn Average Merge Time: %.4f ms per call%n", avgMs);
        System.out.printf("Total Time for %d calls: %.2f ms%n", iterations, duration / 1_000_000.0);
    }
}
