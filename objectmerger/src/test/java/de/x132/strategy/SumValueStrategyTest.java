package de.x132.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.x132.LabeledSource;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SumValueStrategyTest {

    private final SumValueStrategy strategy = new SumValueStrategy();

    static class SalesData {

        public int totalSales;
        public String source;

        SalesData(int totalSales, String source) {
            this.totalSales = totalSales;
            this.source = source;
        }
    }

    static class TestData {
        public Number value;

        TestData(Number value) {
            this.value = value;
        }
    }

    @Test
    @DisplayName("Should instantiate strategy")
    void testInstantiation() {
        assertNotNull(strategy);
    }

    @Test
    @DisplayName("Should sum four sources correctly")
    void testSumFourSources() {
        SalesData amazon = new SalesData(1500, "amazon");
        SalesData shopify = new SalesData(2300, "shopify");
        SalesData ebay = new SalesData(800, "ebay");
        SalesData woocommerce = new SalesData(1200, "woocommerce");

        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("amazon", amazon),
                new LabeledSource<>("shopify", shopify),
                new LabeledSource<>("ebay", ebay),
                new LabeledSource<>("woocommerce", woocommerce));

        Object result = strategy.merge(sources, null, "totalSales");
        assertNotNull(result);
        assertEquals(5800, ((Number) result).intValue());
    }

    @Test
    @DisplayName("Should handle mixed numeric types")
    void testSumMixedTypes() {
        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("s1", new TestData(100)), // Integer
                new LabeledSource<>("s2", new TestData(200.5)), // Double
                new LabeledSource<>("s3", new TestData(150L)), // Long
                new LabeledSource<>("s4", new TestData(50.0f))); // Float

        Object result = strategy.merge(sources, null, "value");
        assertNotNull(result);
        // 100 + 200.5 + 150 + 50.0 = 500.5
        assertEquals(500.5, ((Number) result).doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should handle null values in sources")
    void testSumWithNullValues() {
        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("s1", new TestData(1000)),
                new LabeledSource<>("s2", new TestData(null)),
                new LabeledSource<>("s3", new TestData(2000)),
                new LabeledSource<>("s4", new TestData(500)));

        Object result = strategy.merge(sources, null, "value");
        assertNotNull(result);
        assertEquals(3500, ((Number) result).intValue());
    }

    @Test
    @DisplayName("Should handle single source")
    void testSumSingleSource() {
        SalesData data = new SalesData(1000, "amazon");

        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("amazon", data),
                new LabeledSource<>("shopify", new SalesData(0, "shopify")),
                new LabeledSource<>("ebay", new SalesData(0, "ebay")),
                new LabeledSource<>("woocommerce", new SalesData(0, "woocommerce")));

        Object result = strategy.merge(sources, null, "totalSales");
        assertNotNull(result);
        assertEquals(1000, ((Number) result).intValue());
    }

    @Test
    @DisplayName("Should return zero for empty or no values")
    void testSumEmpty() {
        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("s1", new TestData(null)),
                new LabeledSource<>("s2", new TestData(null)));

        Object result = strategy.merge(sources, null, "value");
        assertNotNull(result);
        assertEquals(0, ((Number) result).intValue());
    }

    @Test
    @DisplayName("Should sum all four sources with passed definition")
    void testSumWithMergeDefinition() {
        SalesData amazon = new SalesData(1500, "amazon");
        SalesData shopify = new SalesData(2300, "shopify");
        SalesData ebay = new SalesData(800, "ebay");
        SalesData woocommerce = new SalesData(1200, "woocommerce");

        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("amazon", amazon),
                new LabeledSource<>("shopify", shopify),
                new LabeledSource<>("ebay", ebay),
                new LabeledSource<>("woocommerce", woocommerce));

        de.x132.FieldDefinition def = new de.x132.FieldDefinition();
        def.setStrategy("sum");

        Object result = strategy.merge(sources, def, "totalSales");
        assertNotNull(result);

        int sum = ((Number) result).intValue();
        assertEquals(5800, sum);
    }
}
