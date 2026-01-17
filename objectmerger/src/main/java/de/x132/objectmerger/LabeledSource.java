package de.x132.objectmerger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LabeledSource<T> {
  private String label;
  private T source;
}
