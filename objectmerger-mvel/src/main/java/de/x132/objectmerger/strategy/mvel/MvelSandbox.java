package de.x132.objectmerger.strategy.mvel;

import java.util.Map;
import java.util.regex.Pattern;
import org.mvel2.ParserContext;

public final class MvelSandbox {

  private static final Pattern[] BLOCKED_EXPRESSION_PATTERNS = {
    Pattern.compile("(?i).*\\bRuntime\\b.*"),
    Pattern.compile("(?i).*\\bProcessBuilder\\b.*"),
    Pattern.compile("(?i).*\\bProcess\\b.*"),
    Pattern.compile("(?i).*\\bSystem\\b.*"),
    Pattern.compile("(?i).*\\bClassLoader\\b.*"),
    Pattern.compile("(?i).*\\bClass\\b.*"),
    Pattern.compile("(?i).*\\bjava\\.lang\\.reflect\\b.*"),
    Pattern.compile("(?i).*\\bMethod\\b.*"),
    Pattern.compile("(?i).*\\bField\\b.*"),
    Pattern.compile("(?i).*\\bConstructor\\b.*"),
    Pattern.compile("(?i).*\\bjava\\.io\\b.*"),
    Pattern.compile("(?i).*\\bjava\\.nio\\b.*"),
    Pattern.compile("(?i).*\\bFile\\b.*"),
    Pattern.compile("(?i).*\\bFileInputStream\\b.*"),
    Pattern.compile("(?i).*\\bFileOutputStream\\b.*"),
    Pattern.compile("(?i).*\\bFileReader\\b.*"),
    Pattern.compile("(?i).*\\bFileWriter\\b.*"),
    Pattern.compile("(?i).*\\bjava\\.net\\b.*"),
    Pattern.compile("(?i).*\\bSocket\\b.*"),
    Pattern.compile("(?i).*\\bURL\\b.*"),
    Pattern.compile("(?i).*\\bHttpURLConnection\\b.*"),
    Pattern.compile("(?i).*\\bScriptEngine\\b.*"),
    Pattern.compile("(?i).*\\bjavax\\.script\\b.*"),
    Pattern.compile("(?i).*\\bThread\\b.*"),
    Pattern.compile("(?i).*\\bThreadGroup\\b.*"),
  };

  private static final Pattern NEW_OBJECT_CREATION_PATTERN = Pattern.compile("\\bnew\\s+[A-Z]");

  private static final String[] BLOCKED_DANGEROUS_CLASSES = {
    "java.lang.Runtime",
    "java.lang.Process",
    "java.lang.Thread",
    "java.io.",
    "java.net.",
    "java.lang.reflect."
  };

  private MvelSandbox() {}

  public static ParserContext createSandboxedParserContext() {
    ParserContext parserContext = new ParserContext();
    parserContext.setStrictTypeEnforcement(false);
    parserContext.setStrongTyping(false);
    return parserContext;
  }

  public static void validateExpression(String expression) {
    if (expression == null || expression.isEmpty()) {
      return;
    }

    rejectBlockedPatterns(expression);
    rejectReflectionAccess(expression);
    rejectObjectCreation(expression);
  }

  public static void validateContextVariables(Map<String, Object> context) {
    if (context == null) {
      return;
    }

    for (Map.Entry<String, Object> entry : context.entrySet()) {
      Object value = entry.getValue();
      if (value == null) {
        continue;
      }
      rejectDangerousContextObject(entry.getKey(), value);
    }
  }

  private static void rejectBlockedPatterns(String expression) {
    for (Pattern pattern : BLOCKED_EXPRESSION_PATTERNS) {
      if (pattern.matcher(expression).find()) {
        throw new SecurityException(
            "MVEL expression blocked by sandbox: references to '"
                + pattern.pattern()
                + "' are not allowed. Expression: "
                + expression);
      }
    }
  }

  private static void rejectReflectionAccess(String expression) {
    if (expression.contains(".getClass(")
        || expression.contains(".class")
        || expression.contains("forName(")
        || expression.contains("newInstance(")) {
      throw new SecurityException(
          "MVEL expression blocked by sandbox: reflection-style access is not allowed. Expression: "
              + expression);
    }
  }

  private static void rejectObjectCreation(String expression) {
    if (NEW_OBJECT_CREATION_PATTERN.matcher(expression).find()) {
      throw new SecurityException(
          "MVEL expression blocked by sandbox: 'new' object creation is not allowed. Expression: "
              + expression);
    }
  }

  private static void rejectDangerousContextObject(String variableName, Object value) {
    String className = value.getClass().getName();
    for (String blockedPrefix : BLOCKED_DANGEROUS_CLASSES) {
      if (className.startsWith(blockedPrefix)) {
        throw new SecurityException(
            "MVEL sandbox violation: context variable '"
                + variableName
                + "' contains a dangerous object of type "
                + className);
      }
    }
  }
}
