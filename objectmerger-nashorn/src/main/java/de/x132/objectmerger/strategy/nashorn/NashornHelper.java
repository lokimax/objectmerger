package de.x132.objectmerger.strategy.nashorn;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/** Helper class to create secure Nashorn contexts. */
public final class NashornHelper {

    private NashornHelper() {}

    /**
     * Creates a new secure Nashorn engine.
     *
     * @return a new {@link ScriptEngine}.
     */
    public static ScriptEngine createSecureEngine() {
        org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory factory =
                new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory();

        ScriptEngine engine =
                factory.getScriptEngine(
                        new String[] {"--no-java", "--no-syntax-extensions"},
                        Thread.currentThread().getContextClassLoader(),
                        className -> false // Restrict ALL Java class loading
                        );

        if (engine == null) {
            throw new IllegalStateException("Nashorn ScriptEngine not found!");
        }

        sanitize(engine);
        return engine;
    }

    private static void sanitize(ScriptEngine engine) {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("Packages", null);
        bindings.put("java", null);
        bindings.put("javax", null);
        bindings.put("org", null);
        bindings.put("com", null);
        bindings.put("net", null);
        bindings.put("io", null);
    }
}
