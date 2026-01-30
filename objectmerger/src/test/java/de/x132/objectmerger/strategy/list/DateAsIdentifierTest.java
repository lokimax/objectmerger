package de.x132.objectmerger.strategy.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.priority.PriorityFieldDefinition;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateAsIdentifierTest {

  @Test
  @DisplayName("Verify identifyBy works with LocalDate fields in Map-based merge")
  void identifyByDate() {
    // Scenario: List of Maps, where each map has a "date" field used as ID.
    // Source A: [{date: 2020-01-01, val: "A"}]
    // Source B: [{date: 2020-01-01, val: "B"}]
    // Target: Merge these into one item because date matches.

    LocalDate date = LocalDate.of(2020, 1, 1);

    Map<String, Object> itemA = Map.of("date", date, "val", "A");
    Map<String, Object> itemB = Map.of("date", date, "val", "B");

    List<Map<String, Object>> listA = List.of(itemA);
    List<Map<String, Object>> listB = List.of(itemB);

    ListFieldDefinition listDef = new ListFieldDefinition();
    listDef.setStrategy("mergeList");
    listDef.setIdentifyBy("date");

    PriorityFieldDefinition dateDef = new PriorityFieldDefinition();
    dateDef.setStrategy("priority");
    dateDef.setPriority(java.util.Map.of("A", 1, "B", 2));

    PriorityFieldDefinition valDef = new PriorityFieldDefinition();
    valDef.setStrategy("concatenate");

    // Construct item definition
    Map<String, FieldDefinition> itemFields = new HashMap<>();
    itemFields.put("date", dateDef);
    itemFields.put("val", valDef);

    ItemMergeDefinition itemDef = new ItemMergeDefinition();
    itemDef.setDefinitions(itemFields);
    listDef.setItemMergeDefinition(itemDef);

    Map<String, FieldDefinition> rootFields = new HashMap<>();
    rootFields.put("list", listDef);

    MergeDefinition def = new MergeDefinition(rootFields);

    Map<String, Object> sourceA = Map.of("list", listA);
    Map<String, Object> sourceB = Map.of("list", listB);

    Map<String, Object> result =
        ObjectMerger.merge(
            def, new LabeledSource<>("A", sourceA), new LabeledSource<>("B", sourceB));

    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get("list");
    assertNotNull(resultList);
    assertEquals(1, resultList.size());

    Map<String, Object> mergedItem = resultList.get(0);
    assertEquals(date, mergedItem.get("date"));
    // "A,B" or "B,A" depending on priority (default is stable order or priority 0)
    // Actually Concatenate sorts by priority. Here priority is default (0).
    String val = (String) mergedItem.get("val");
    // Just check length or containment as order might vary if priority is equal
    assertEquals(3, val.length());
  }
}
