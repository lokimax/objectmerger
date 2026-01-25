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

    for (String key : data.keySet()) {
      definitions.put(key, new StandardFieldDefinition());
    }

    return new MergeDefinition(definitions);
  }
}
