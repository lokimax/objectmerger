package de.x132.objectmerger.strategy.sum;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import de.x132.objectmerger.strategy.MergeStrategy;
import java.math.BigDecimal;
import java.util.List;

public class SumValueStrategy implements MergeStrategy<Number, FieldDefinition> {

  public static final String NAME = "sum";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Class<FieldDefinition> getConfigurationClass() {
    return FieldDefinition.class;
  }

  @Override
  public Number merge(List<LabeledSource<?>> sources, FieldDefinition fieldDef, String fieldName) {
    BigDecimal sum = BigDecimal.ZERO;
    boolean hasValue = false;

    for (LabeledSource<?> source : sources) {
      Object value = ObjectMerger.getFieldValue(source.getSource(), fieldName);

      if (value != null) {
        hasValue = true;
        BigDecimal numericValue = convertToBigDecimal(value);
        if (numericValue != null) {
          sum = sum.add(numericValue);
        }
      }
    }

    if (!hasValue) {
      return fieldDef != null ? (Number) fieldDef.getDefaultValue() : 0;
    }

    // Rückgabe als Integer oder Long wenn möglich, sonst BigDecimal
    if (sum.scale() <= 0) {
      try {
        return sum.intValueExact();
      } catch (ArithmeticException e) {
        try {
          return sum.longValueExact();
        } catch (ArithmeticException ex) {
          return sum;
        }
      }
    }

    return sum.doubleValue();
  }

  private BigDecimal convertToBigDecimal(Object value) {
    if (value instanceof BigDecimal) {
      return (BigDecimal) value;
    } else if (value instanceof Integer) {
      return BigDecimal.valueOf((Integer) value);
    } else if (value instanceof Long) {
      return BigDecimal.valueOf((Long) value);
    } else if (value instanceof Double) {
      return BigDecimal.valueOf((Double) value);
    } else if (value instanceof Float) {
      return BigDecimal.valueOf((Float) value);
    } else if (value instanceof String) {
      try {
        return new BigDecimal((String) value);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }
}
