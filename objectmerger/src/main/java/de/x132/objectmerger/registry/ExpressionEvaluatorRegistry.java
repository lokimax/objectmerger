package de.x132.objectmerger.registry;

import de.x132.objectmerger.expression.ExpressionEvaluator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExpressionEvaluatorRegistry {
    private static final ExpressionEvaluatorRegistry INSTANCE = new ExpressionEvaluatorRegistry();
    private final Map<String, ExpressionEvaluator> evaluators = new HashMap<>();

    private ExpressionEvaluatorRegistry() {
        java.util.ServiceLoader<ExpressionEvaluator> loader = java.util.ServiceLoader.load(ExpressionEvaluator.class);
        for (ExpressionEvaluator evaluator : loader) {
            register(evaluator);
        }
    }

    public static ExpressionEvaluatorRegistry getInstance() {
        return INSTANCE;
    }

    public void register(ExpressionEvaluator evaluator) {
        evaluators.put(evaluator.getName(), evaluator);
        log.info("Registered expression evaluator: {}", evaluator.getName());
    }

    public Optional<ExpressionEvaluator> getEvaluator(String name) {
        return Optional.ofNullable(evaluators.get(name));
    }

    public Optional<ExpressionEvaluator> getEvaluator() {
        if (evaluators.containsKey("mvel")) {
            return Optional.of(evaluators.get("mvel"));
        }
        return evaluators.values().stream().findFirst();
    }
}
