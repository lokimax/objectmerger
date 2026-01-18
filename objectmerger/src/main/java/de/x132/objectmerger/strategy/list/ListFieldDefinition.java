package de.x132.objectmerger.strategy.list;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.ItemMergeDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ListFieldDefinition extends FieldDefinition {
  private String identifyBy;
  private ItemMergeDefinition itemMergeDefinition;
}
