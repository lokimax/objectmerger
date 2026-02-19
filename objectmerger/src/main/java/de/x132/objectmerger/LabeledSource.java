package de.x132.objectmerger;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabeledSource<T> {
    private String label;
    private T source;
}
