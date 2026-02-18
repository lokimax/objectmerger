package de.x132.objectmerger.mvel;

import de.x132.objectmerger.expression.ExpressionEvaluator;
import de.x132.objectmerger.strategy.mvel.MvelSandbox;
import java.io.Serializable;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;

@Slf4j
public class MvelEvaluator implements ExpressionEvaluator {

    @Override
    public String getName() {
        return "mvel";
    }

    @Override
    public boolean evaluateBoolean(String expression, Map<String, Object> context) {
         Object result = evaluate(expression, context);
         return Boolean.TRUE.equals(result);
    }

    @Override
    public Object evaluate(String expression, Map<String, Object> context) {
        validate(expression);
        MvelSandbox.validateContextVariables(context);
        
        Serializable compiled = MVEL.compileExpression(expression, MvelSandbox.createSandboxedParserContext());
        return MVEL.executeExpression(compiled, context);
    }

    @Override
    public void validate(String expression) {
        MvelSandbox.validateExpression(expression);
    }
}
