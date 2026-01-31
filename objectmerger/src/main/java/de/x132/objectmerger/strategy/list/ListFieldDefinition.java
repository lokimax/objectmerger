package de.x132.objectmerger.strategy.list;

import de.x132.objectmerger.ItemMergeDefinition;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.config.ListConfig;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListFieldDefinition extends FieldDefinition<Object> implements ListConfig {
  private boolean deduplicate;
  private String identifyBy;
  private ItemMergeDefinition itemMergeDefinition;
  private List<String> keyOriginLabels;
  private boolean requirePresenceInAllKeyOrigins;
}
