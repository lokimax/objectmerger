package de.x132.objectmerger;

import lombok.Getter;

@Getter
public class LabeledSource<T> {
  private String label;
  private T source;

  public LabeledSource(String label, T source) {
    this.label = label;
    this.source = source;
  }

  public String getLabel() {
    return label;
  }

  public T getSource() {
    return source;
  }
}
