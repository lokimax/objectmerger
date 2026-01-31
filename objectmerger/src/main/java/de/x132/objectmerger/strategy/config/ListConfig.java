package de.x132.objectmerger.strategy.config;

import de.x132.objectmerger.ItemMergeDefinition;
import java.util.List;

public interface ListConfig {
  boolean isDeduplicate();

  String getIdentifyBy();

  ItemMergeDefinition getItemMergeDefinition();

  List<String> getKeyOriginLabels();

  boolean isRequirePresenceInAllKeyOrigins();
}
