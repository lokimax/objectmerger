package de.x132.objectmerger.strategy.map;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapFieldDefinition extends FieldDefinition<Object> {
  private String keyStrategy;
  private List<String> keyTemplateSources;
  private ItemMergeDefinition itemMergeDefinition;
}
