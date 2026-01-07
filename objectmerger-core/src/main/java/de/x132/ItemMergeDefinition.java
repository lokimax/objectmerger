package de.x132;

import java.util.Map;
import lombok.Data;

@Data
public class ItemMergeDefinition {
  private String targetClass;
  private Map<String, FieldDefinition> definitions;
}
