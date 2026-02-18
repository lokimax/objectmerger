package de.x132.objectmerger.expression;

import java.util.Map;

public interface ExpressionEvaluator {
  boolean evaluateBoolean(String expression, Map<String, Object> context);

  Object evaluate(String expression, Map<String, Object> context);

  void validate(String expression);

  String getName();
}
