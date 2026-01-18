package de.x132.objectmerger.strategy.list;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.ItemMergeDefinition;

public class ListFieldDefinition extends FieldDefinition {
  private boolean deduplicate;
  private String identifyBy;
  private ItemMergeDefinition itemMergeDefinition;

  public boolean isDeduplicate() {
    return deduplicate;
  }

  public void setDeduplicate(boolean deduplicate) {
    this.deduplicate = deduplicate;
  }

  public String getIdentifyBy() {
    return identifyBy;
  }

  public void setIdentifyBy(String identifyBy) {
    this.identifyBy = identifyBy;
  }

  public ItemMergeDefinition getItemMergeDefinition() {
    return itemMergeDefinition;
  }

  public void setItemMergeDefinition(ItemMergeDefinition itemMergeDefinition) {
    this.itemMergeDefinition = itemMergeDefinition;
  }
}
