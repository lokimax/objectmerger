package de.x132.objectmerger;

import java.util.Map;

public class MergeDefinition {
  private Map<String, FieldDefinition> definitions;

  public Map<String, FieldDefinition> getDefinitions() {
    return definitions;
  }

  public void setDefinitions(Map<String, FieldDefinition> definitions) {
    this.definitions = definitions;
  }
}
