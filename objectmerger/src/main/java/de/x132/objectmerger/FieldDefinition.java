package de.x132.objectmerger;

import java.util.Map;
import lombok.Data;

@Data
public class FieldDefinition {
  private Map<String, Integer> priority;
  private String strategy;
  private String identifyBy;
  private ItemMergeDefinition itemMergeDefinition;
  private java.util.List<String> keyTemplateSources;
  private Object defaultValue;
}
