package de.x132.objectmerger.strategy.priority;

import de.x132.objectmerger.FieldDefinition;
import java.util.Map;

public class PriorityFieldDefinition extends FieldDefinition {
  private Map<String, Integer> priority;

  public Map<String, Integer> getPriority() {
    return priority;
  }

  public void setPriority(Map<String, Integer> priority) {
    this.priority = priority;
  }
}
