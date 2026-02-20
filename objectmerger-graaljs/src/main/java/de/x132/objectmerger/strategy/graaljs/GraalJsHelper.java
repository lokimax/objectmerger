package de.x132.objectmerger.strategy.graaljs;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

public class GraalJsHelper {

    private static final Engine ENGINE = Engine.newBuilder().build();

    public static Context createSecureContext() {
        Context context =
                Context.newBuilder("js")
                        .engine(ENGINE)
                        .allowHostAccess(HostAccess.ALL)
                        .allowHostClassLookup(s -> false) // Secure by default
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
