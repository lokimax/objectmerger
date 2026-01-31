package de.x132.objectmerger;

import de.x132.objectmerger.strategy.FieldDefinition;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemMergeDefinition {
  private String targetClass;
  private Map<String, FieldDefinition<?>> definitions;
}
