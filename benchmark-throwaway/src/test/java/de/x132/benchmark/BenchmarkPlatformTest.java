package de.x132.benchmark;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.strategy.graaljs.GraalJsFieldDefinition;
import de.x132.objectmerger.strategy.graaljs.GraalJsMergeStrategy;
import de.x132.objectmerger.strategy.mvel.MvelFieldDefinition;
import de.x132.objectmerger.strategy.mvel.MvelMergeStrategy;
import de.x132.objectmerger.strategy.nashorn.NashornFieldDefinition;
import de.x132.objectmerger.strategy.nashorn.NashornMergeStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class BenchmarkPlatformTest {

    @Test
    public void benchmarkGraalJsVsMvelVsNashorn() {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        GraalJsMergeStrategy graalStrategy = new GraalJsMergeStrategy();
        MvelMergeStrategy mvelStrategy = new MvelMergeStrategy();
        NashornMergeStrategy nashornStrategy = new NashornMergeStrategy();

        LabeledSource<Object> source = new LabeledSource<>("test", Map.of("val", "value"));
        List<LabeledSource<?>> sources = Collections.singletonList(source);

        // GraalJS Setup
        GraalJsFieldDefinition graalConfig =
                GraalJsFieldDefinition.builder().expression("sources.test.val").build();

        // MVEL Setup
        MvelFieldDefinition mvelConfig =
                MvelFieldDefinition.builder().expression("sources['test']['val']").build();

        // Nashorn Setup
        NashornFieldDefinition nashornConfig =
                NashornFieldDefinition.builder().expression("sources.test.val").build();

        // Warmup (to trigger JIT before real measurements)
        System.out.println("Warming up engines...");
        for (int i = 0; i < 20000; i++) {
            graalStrategy.merge(sources, graalConfig, "testField");
            mvelStrategy.merge(sources, mvelConfig, "testField");
            nashornStrategy.merge(sources, nashornConfig, "testField");
        }

        int[] sizes = {
            10_000, 50_000, 100_000
        }; // Reduced size so it doesn't take forever during testing. 1 mil took too long for
        // GraalJS/Nashorn

        System.out.println("\n=== Benchmark Data (Total Time in ms) ===");
        System.out.println("Size,MVEL,GraalJS,Nashorn");

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

            // Measure Nashorn
            long startNashorn = System.nanoTime();
            for (int i = 0; i < size; i++) {
                nashornStrategy.merge(sources, nashornConfig, "testField");
            }
            long durationNashorn = System.nanoTime() - startNashorn;
            double msNashorn = durationNashorn / 1_000_000.0;

            System.out.printf("%d,%.2f,%.2f,%.2f%n", size, msMvel, msGraal, msNashorn);
        }
    }
}
