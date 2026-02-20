package de.x132.objectmerger.strategy.graaljs;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

/**
 * Helper class to create secure GraalJS contexts.
 *
 * <p>Ensures that potentially dangerous global objects (Packages, java, etc.) are removed to
 * prevent unauthorized access to the host system.
 */
public final class GraalJsHelper {

    private static final Engine ENGINE = Engine.newBuilder().build();

    // Private constructor to prevent instantiation
    private GraalJsHelper() {}

    /**
     * Creates a new secure GraalJS context.
     *
     * @return a new secure {@link Context}. The caller is responsible for closing it.
     */
    public static Context createSecureContext() {
        Context context =
                Context.newBuilder("js")
                        .engine(ENGINE)
                        .allowHostAccess(HostAccess.ALL)
                        .allowHostClassLookup(s -> false) // Restricts explici
                        .build();
        sanitize(context);
        return context;
    }

    private static void sanitize(Context context) {
        Value bindings = context.getBindings("js");
        // Remove potential global access points for security
        bindings.putMember("Packages", null);
        bindings.putMember("java", null);
        bindings.putMember("javax", null);
        bindings.putMember("org", null);
        bindings.putMember("com", null);
        bindings.putMember("net", null);
        bindings.putMember("io", null);
    }
}
