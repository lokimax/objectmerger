package de.x132.doc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.average.AverageValueStrategy;
import de.x132.objectmerger.strategy.concatenate.ConcatenateStrategy;
import de.x132.objectmerger.strategy.conditional.ConditionCase;
import de.x132.objectmerger.strategy.conditional.ConditionalFieldDefinition;
import de.x132.objectmerger.strategy.conditional.ConditionalMergeStrategy;
import de.x132.objectmerger.strategy.list.ListFieldDefinition;
import de.x132.objectmerger.strategy.list.ListMergeStrategy;
import de.x132.objectmerger.strategy.map.MapFieldDefinition;
import de.x132.objectmerger.strategy.map.MapMergeStrategy;
import de.x132.objectmerger.strategy.maximum.MaximumValueStrategy;
import de.x132.objectmerger.strategy.minimum.MinimumValueStrategy;
import de.x132.objectmerger.strategy.mvel.MvelFieldDefinition;
import de.x132.objectmerger.strategy.mvel.MvelMergeStrategy;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityMergeStrategy;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardMergeStrategy;
import de.x132.objectmerger.strategy.sum.SumValueStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DocumentationVerificationTest {

  @Test
  void verifyStandardStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("title", "Legacy System");

    Map<String, Object> source2 = new HashMap<>();
    source2.put("title", "New System");

    StandardFieldDefinition<String> titleDef =
        StandardFieldDefinition.<String>builder()
            .strategy(StandardMergeStrategy.NAME)
            .defaultValue("Unknown")
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("title", titleDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals("Legacy System", result.get("title"));
  }

  @Test
  void verifySumStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("score", 10);

    Map<String, Object> source2 = new HashMap<>();
    source2.put("score", 20);

    StandardFieldDefinition<Number> scoreDef =
        StandardFieldDefinition.<Number>builder()
            .strategy(SumValueStrategy.NAME)
            .defaultValue(0)
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("score", scoreDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals(30, result.get("score"));
  }

  @Test
  void verifyConcatenateStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("tags", "tag1");

    Map<String, Object> source2 = new HashMap<>();
    source2.put("tags", "tag2");

    StandardFieldDefinition<String> tagsDef =
        StandardFieldDefinition.<String>builder()
            .strategy(ConcatenateStrategy.NAME)
            .defaultValue("")
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("tags", tagsDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals("tag1,tag2", result.get("tags"));
  }

  @Test
  void verifyMaximumStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("level", 5);

    Map<String, Object> source2 = new HashMap<>();
    source2.put("level", 8);

    StandardFieldDefinition<Number> levelDef =
        StandardFieldDefinition.<Number>builder()
            .strategy(MaximumValueStrategy.NAME)
            .defaultValue(0)
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("level", levelDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals(8, result.get("level"));
  }

  @Test
  void verifyMinimumStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("price", 99.99);

    Map<String, Object> source2 = new HashMap<>();
    source2.put("price", 45.50);

    StandardFieldDefinition<Number> priceDef =
        StandardFieldDefinition.<Number>builder()
            .strategy(MinimumValueStrategy.NAME)
            .defaultValue(0.0)
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("price", priceDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals(45.50, result.get("price"));
  }

  @Test
  void verifyAverageStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("rating", 4.0);

    Map<String, Object> source2 = new HashMap<>();
    source2.put("rating", 5.0);

    StandardFieldDefinition<Number> ratingDef =
        StandardFieldDefinition.<Number>builder()
            .strategy(AverageValueStrategy.NAME)
            .defaultValue(0.0)
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("rating", ratingDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals(4.5, result.get("rating"));
  }

  @Test
  void verifyPriorityStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("status", "DRAFT");

    Map<String, Object> source2 = new HashMap<>();
    source2.put("status", "PUBLISHED");

    Map<String, Integer> priorities = new HashMap<>();
    priorities.put("json2", 1);
    priorities.put("json1", 2);

    PriorityFieldDefinition<String> statusDef =
        PriorityFieldDefinition.<String>builder()
            .strategy(PriorityMergeStrategy.NAME)
            .defaultValue("UNKNOWN")
            .priority(priorities)
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("status", statusDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals("PUBLISHED", result.get("status"));
  }

  @Test
  void verifyConditionalStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("age", 18);
    source1.put("category", "adult");

    Map<String, Object> source2 = new HashMap<>();
    source2.put("age", 18);
    source2.put("category", "minor");

    ConditionCase<String> adultCase = new ConditionCase<>();
    adultCase.setCondition("values['json1'] == 'adult'");

    Map<String, Integer> p = new HashMap<>();
    p.put("json1", 1);
    p.put("json2", 2);

    PriorityFieldDefinition<String> priorityDef =
        PriorityFieldDefinition.<String>builder()
            .strategy(PriorityMergeStrategy.NAME)
            .priority(p)
            .build();

    adultCase.setUseStrategy(priorityDef);

    List<ConditionCase<String>> cases = new ArrayList<>();
    cases.add(adultCase);

    ConditionalFieldDefinition<String> categoryDef =
        ConditionalFieldDefinition.<String>builder()
            .strategy(ConditionalMergeStrategy.NAME)
            .defaultValue("unknown")
            .cases(cases)
            .build();

    StandardFieldDefinition<String> defaultDef =
        StandardFieldDefinition.<String>builder()
            .strategy(StandardMergeStrategy.NAME)
            .defaultValue("fallback")
            .build();
    categoryDef.setDefaultStrategy(defaultDef);

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("category", categoryDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    assertEquals("adult", result.get("category"));
  }

  @Test
  void verifyMvelStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    source1.put("price", 100);
    source1.put("discount", 0.1);

    Map<String, Object> source2 = new HashMap<>();

    MvelFieldDefinition finalPriceDef =
        MvelFieldDefinition.builder()
            .strategy(MvelMergeStrategy.NAME)
            .expression("sources['json1']['price'] * (1.0 - sources['json1']['discount'])")
            .defaultValue(0.0)
            .build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("finalPrice", finalPriceDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    Object val = result.get("finalPrice");
    assertEquals(90.0, ((Number) val).doubleValue(), 0.01);
  }

  @SuppressWarnings("unchecked")
  @Test
  void verifyListStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    List<Map<String, Object>> list1 = new ArrayList<>();
    Map<String, Object> item1 = new HashMap<>();
    item1.put("id", "1");
    item1.put("name", "Item 1");
    list1.add(item1);
    source1.put("items", list1);

    Map<String, Object> source2 = new HashMap<>();
    List<Map<String, Object>> list2 = new ArrayList<>();
    Map<String, Object> item2 = new HashMap<>();
    item2.put("id", "2");
    item2.put("name", "Item 2");
    list2.add(item2);
    source2.put("items", list2);

    ListFieldDefinition<List<Object>> itemsDef =
        ListFieldDefinition.<List<Object>>builder()
            .strategy(ListMergeStrategy.NAME)
            .identifyBy("id")
            .build();

    ItemMergeDefinition itemDef = new ItemMergeDefinition();
    Map<String, FieldDefinition<?>> itemFields = new HashMap<>();

    StandardFieldDefinition<String> nameDef =
        StandardFieldDefinition.<String>builder()
            .strategy(StandardMergeStrategy.NAME)
            .defaultValue("Unknown Item")
            .build();
    itemFields.put("name", nameDef);

    itemDef.setDefinitions(itemFields);
    itemsDef.setItemMergeDefinition(itemDef);

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("items", itemsDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get("items");
    assertEquals(2, resultList.size());
    boolean hasItem1 = resultList.stream().anyMatch(m -> "Item 1".equals(m.get("name")));
    boolean hasItem2 = resultList.stream().anyMatch(m -> "Item 2".equals(m.get("name")));
    assertTrue(hasItem1);
    assertTrue(hasItem2);
  }

  @SuppressWarnings("unchecked")
  @Test
  void verifyMapStrategy() {
    Map<String, Object> source1 = new HashMap<>();
    Map<String, Object> map1 = new HashMap<>();
    map1.put("en", "Hello");
    source1.put("translations", map1);

    Map<String, Object> source2 = new HashMap<>();
    Map<String, Object> map2 = new HashMap<>();
    map2.put("de", "Hallo");
    source2.put("translations", map2);

    MapFieldDefinition<Map<Object, Object>> transDef =
        MapFieldDefinition.<Map<Object, Object>>builder().strategy(MapMergeStrategy.NAME).build();

    Map<String, FieldDefinition<?>> definitions = new HashMap<>();
    definitions.put("translations", transDef);

    MergeDefinition mergeDefinition = new MergeDefinition(definitions);

    Map<String, Object> result =
        ObjectMerger.merge(
            mergeDefinition,
            new LabeledSource<>("json1", source1),
            new LabeledSource<>("json2", source2));

    Map<String, Object> resultMap = (Map<String, Object>) result.get("translations");
    assertEquals("Hello", resultMap.get("en"));
    assertEquals("Hallo", resultMap.get("de"));
  }
}
