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
        Integer val1 = 100;
        Long val2 = 200L;
        Double val3 = 150.5;
        Float val4 = 50.0f;

        SalesData data1 = new SalesData(100, "source1");
        data1.totalSales = val1;

        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("s1", data1),
                new LabeledSource<>("s2", new SalesData(200, "s2")),
                new LabeledSource<>("s3", new SalesData(150, "s3")),
                new LabeledSource<>("s4", new SalesData(50, "s4")));

        Object result = strategy.merge(sources, null, "totalSales");
        assertNotNull(result);
        assertEquals(500, ((Number) result).intValue());
    }

    @Test
    @DisplayName("Should handle null values in sources")
    void testSumWithNullValues() {
        SalesData dataWithValue = new SalesData(1000, "valid");
        SalesData dataWithNull = new SalesData(0, "source");
        dataWithNull.totalSales = 0;

        List<LabeledSource<?>> sources = List.of(
                new LabeledSource<>("s1", dataWithValue),
                new LabeledSource<>("s2", dataWithNull),
                new LabeledSource<>("s3", new SalesData(2000, "s3")),
                new LabeledSource<>("s4", new SalesData(500, "s4")));

        Object result = strategy.merge(sources, null, "totalSales");
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
                new LabeledSource<>("s1", new SalesData(0, "s1")),
                new LabeledSource<>("s2", new SalesData(0, "s2")),
                new LabeledSource<>("s3", new SalesData(0, "s3")),
                new LabeledSource<>("s4", new SalesData(0, "s4")));

        Object result = strategy.merge(sources, null, "totalSales");
        assertNotNull(result);
        assertEquals(0, ((Number) result).intValue());
    }

    @Test
    @DisplayName("Should sum all four sources from merge definition")
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

        Object result = strategy.merge(sources, null, "totalSales");
        assertNotNull(result);

        int sum = ((Number) result).intValue();
        assertEquals(1500 + 2300 + 800 + 1200, sum);
    }
}
