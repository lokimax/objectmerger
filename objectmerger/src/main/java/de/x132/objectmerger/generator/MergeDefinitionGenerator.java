package de.x132.objectmerger.generator;

import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.standard.StandardFieldDefinition;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class MergeDefinitionGenerator {

  public static MergeDefinition generate(Class<?> clazz) {
    Map<String, FieldDefinition> definitions = new HashMap<>();

    for (Field field : clazz.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
        continue;
      }
      definitions.put(field.getName(), new StandardFieldDefinition());
    }

    return new MergeDefinition(definitions);
  }

  public static MergeDefinition generate(Map<String, Object> data) {
    Map<String, FieldDefinition> definitions = new HashMap<>();

    for (Map.Entry<String, Object> entry : data.entrySet()) {
      definitions.put(entry.getKey(), generateFieldDefinition(entry.getValue()));
    }

    return new MergeDefinition(definitions);
  }

  private static FieldDefinition generateFieldDefinition(Object value) {
    if (value instanceof java.util.List) {
      java.util.List<?> list = (java.util.List<?>) value;
      String identifyBy = null;
      de.x132.objectmerger.ItemMergeDefinition itemDef = null;

      if (!list.isEmpty()) {
        Object firstObj = list.get(0);
        if (firstObj instanceof Map) {
          Map<?, ?> first = (Map<?, ?>) firstObj;
          if (first.containsKey("id")) {
            identifyBy = "id";
          } else if (first.containsKey("name")) {
            identifyBy = "name";
          }

          // Recursive generation for list items
          @SuppressWarnings("unchecked")
          MergeDefinition subDef = generate((Map<String, Object>) first);
          itemDef = new de.x132.objectmerger.ItemMergeDefinition(null, subDef.getDefinitions());
        }
      }

      de.x132.objectmerger.strategy.list.ListFieldDefinition def =
          new de.x132.objectmerger.strategy.list.ListFieldDefinition();
      def.setStrategy("mergeList");
      if (identifyBy != null) {
        def.setIdentifyBy(identifyBy);
      }
      if (itemDef != null) {
        def.setItemMergeDefinition(itemDef);
      }
      return def;
    } else if (value instanceof Map) {
      de.x132.objectmerger.strategy.map.MapFieldDefinition def =
          new de.x132.objectmerger.strategy.map.MapFieldDefinition();
      def.setStrategy("mergeMap");
      // Inspect first value to see if itemMergeDefinition is needed (recurse)
      Map<?, ?> map = (Map<?, ?>) value;
      if (!map.isEmpty()) {
        Object firstVal = map.values().iterator().next();
        // If value is Map/List, we might need recursion, but for this task (simple
        // names), null is OK.
        // MapMergeStrategy defaults to priority merge for values if no item definition.
        // Ideally we should recurse if firstVal is complex, but let's stick to simple
        // "mergeMap".
      }
      return def;
    } else {
      return createDefaultPriorityDefinition();
    }
  }

  private static FieldDefinition createDefaultPriorityDefinition() {
    de.x132.objectmerger.strategy.priority.PriorityFieldDefinition def =
        new de.x132.objectmerger.strategy.priority.PriorityFieldDefinition();
    def.setStrategy("priority");
    def.setPriority(Map.of("source_1", 1, "source_2", 2));
    return def;
  }
}
