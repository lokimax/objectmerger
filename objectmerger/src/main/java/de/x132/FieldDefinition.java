package de.x132;

import java.util.Map;
import lombok.Data;

@Data
public class FieldDefinition {
  private Map<String, Integer> priority;
  private String strategy;
  private String identifyBy;
  private ItemMergeDefinition itemMergeDefinition;
  private Object defaultValue;
}
