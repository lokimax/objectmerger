package de.x132.objectmerger.strategy.map;

import de.x132.objectmerger.FieldDefinition;
import de.x132.objectmerger.ItemMergeDefinition;
import java.util.List;

public class MapFieldDefinition extends FieldDefinition {
  private String keyStrategy;
  private List<String> keyTemplateSources;
  private ItemMergeDefinition itemMergeDefinition;

  public String getKeyStrategy() {
    return keyStrategy;
  }

  public void setKeyStrategy(String keyStrategy) {
    this.keyStrategy = keyStrategy;
  }

  public List<String> getKeyTemplateSources() {
    return keyTemplateSources;
  }

  public void setKeyTemplateSources(List<String> keyTemplateSources) {
    this.keyTemplateSources = keyTemplateSources;
  }

  public ItemMergeDefinition getItemMergeDefinition() {
    return itemMergeDefinition;
  }

  public void setItemMergeDefinition(ItemMergeDefinition itemMergeDefinition) {
    this.itemMergeDefinition = itemMergeDefinition;
  }
}
