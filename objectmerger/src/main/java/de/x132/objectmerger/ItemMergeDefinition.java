package de.x132.objectmerger;

import java.util.Map;

public class ItemMergeDefinition {
  private String targetClass;
  private Map<String, FieldDefinition> definitions;

  public String getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(String targetClass) {
    this.targetClass = targetClass;
  }

  public Map<String, FieldDefinition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(Map<String, FieldDefinition> definitions) {
    this.definitions = definitions;
  }
}
