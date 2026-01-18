package de.x132.objectmerger.strategy.map;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.ItemMergeDefinition;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MapFieldDefinition extends FieldDefinition {
  private ItemMergeDefinition itemMergeDefinition;
  private List<String> keyTemplateSources;
}
