package de.x132.objectmerger;

import java.util.Map;
import lombok.Data;

@Data
public class ItemMergeDefinition {
  private String targetClass;
  private Map<String, FieldDefinition> definitions;
}
