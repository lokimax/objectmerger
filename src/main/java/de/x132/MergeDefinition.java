package de.x132;

import java.util.Map;
import lombok.Data;

@Data
public class MergeDefinition {
  private Map<String, FieldDefinition> definitions;
}
