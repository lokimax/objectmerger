package de.x132.benchmark;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.graaljs.GraalJsFieldDefinition;
import de.x132.objectmerger.strategy.graaljs.GraalJsMergeStrategy;
import de.x132.objectmerger.strategy.mvel.MvelFieldDefinition;
import de.x132.objectmerger.strategy.mvel.MvelMergeStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class BenchmarkPlatformTest {

    @Test
    public void benchmarkGraalJsVsMvel() {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        GraalJsMergeStrategy graalStrategy = new GraalJsMergeStrategy();
        MvelMergeStrategy mvelStrategy = new MvelMergeStrategy();

        LabeledSource<Object> source = new LabeledSource<>("test", Map.of("val", "value"));
        List<LabeledSource<?>> sources = Collections.singletonList(source);

        // GraalJS Setup
        GraalJsFieldDefinition graalConfig =
                GraalJsFieldDefinition.builder().expression("sources.test.val").build();

        // MVEL Setup
        MvelFieldDefinition mvelConfig =
                MvelFieldDefinition.builder().expression("sources['test']['val']").build();

        // Warmup (to trigger JIT before real measurements)
        System.out.println("Warming up engines...");
        for (int i = 0; i < 20000; i++) {
            graalStrategy.merge(sources, graalConfig, "testField");
            mvelStrategy.merge(sources, mvelConfig, "testField");
        }

        int[] sizes = {10_000, 50_000, 100_000, 250_000, 500_000, 1_000_000};

        System.out.println("=== Benchmark Data (Total Time in ms) ===");
        System.out.println("Size,MVEL,GraalJS");

        for (int size : sizes) {
            // Measure MVEL
            long startMvel = System.nanoTime();
            for (int i = 0; i < size; i++) {
                mvelStrategy.merge(sources, mvelConfig, "testField");
            }
            long durationMvel = System.nanoTime() - startMvel;
            double msMvel = durationMvel / 1_000_000.0;

            // Measure GraalJS
            long startGraal = System.nanoTime();
            for (int i = 0; i < size; i++) {
                graalStrategy.merge(sources, graalConfig, "testField");
            }
            long durationGraal = System.nanoTime() - startGraal;
            double msGraal = durationGraal / 1_000_000.0;

            System.out.printf("%d,%.2f,%.2f%n", size, msMvel, msGraal);
        }
    }
}
