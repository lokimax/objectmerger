package de.x132.objectmerger.strategy.list;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListFieldDefinition extends FieldDefinition {
  private boolean deduplicate;
  private String identifyBy;
  private ItemMergeDefinition itemMergeDefinition;
}
