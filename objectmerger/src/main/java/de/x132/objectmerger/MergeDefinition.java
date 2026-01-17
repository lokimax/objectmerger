package de.x132.objectmerger;

import java.util.Map;
import lombok.Data;

@Data
public class MergeDefinition {
  private Map<String, FieldDefinition> definitions;
}
