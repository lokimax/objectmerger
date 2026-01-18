package de.x132.objectmerger;

public abstract class FieldDefinition {
  private String strategy;
  private Object defaultValue;

  public String getStrategy() {
    return strategy;
  }

  public void setStrategy(String strategy) {
    this.strategy = strategy;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }
}
