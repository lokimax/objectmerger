package de.x132.objectmerger.strategy.nashorn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.x132.objectmerger.LabeledSource;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Nashorn Sandbox Security Tests")
class NashornSandboxTest {

    private final NashornMergeStrategy strategy = new NashornMergeStrategy();

    // Default value to expect when expression is blocked
    private static final String BLOCKED_VALUE = "BLOCKED";

    private List<LabeledSource<?>> dummySources() {
        return Arrays.asList(new LabeledSource<>("a", 10), new LabeledSource<>("b", 20));
    }

    private void assertBlocked(String expression) {
        NashornFieldDefinition fieldDef =
                NashornFieldDefinition.builder()
                        .expression(expression)
                        .defaultValue(BLOCKED_VALUE)
                        .build();

        Object result = strategy.merge(dummySources(), fieldDef, "test");
        assertEquals(
                BLOCKED_VALUE,
                result,
                "Expression SHOULD have failed/blocked but didn't: " + expression);
    }

    private void assertAllowed(String expression, Object expectedValue) {
        NashornFieldDefinition fieldDef =
                NashornFieldDefinition.builder()
                        .expression(expression)
                        .defaultValue(BLOCKED_VALUE)
                        .build();

        Object result = strategy.merge(dummySources(), fieldDef, "test");
        assertEquals(expectedValue, result, "Expression SHOULD have succeeded: " + expression);
    }

    @Nested
    @DisplayName("Legitimate expressions")
    class LegitimateExpressionsAreAllowed {
        @Test
        void arithmetic() {
            // Nashorn returns Integer for simple int math usually.
            assertAllowed("1 + 1", 2);
        }
    }

    @Nested
    @DisplayName("Java Type Access must be blocked")
    class JavaAccessBlocked {
        @Test
        void blocksJavaType() {
            assertBlocked("Java.type('java.lang.System')");
        }

        @Test
        void blocksPackages() {
            assertBlocked("Packages.java.lang.System");
        }

        @Test
        void blocksFullyQualified() {
            assertBlocked("java.lang.System.exit(0)");
        }
    }

    @Nested
    @DisplayName("Reflection/Class access must be blocked")
    class ReflectionBlocked {
        @Test
        void blocksGetClass() {
            assertBlocked("sources.getClass().forName('java.lang.Runtime')");
        }

        @Test
        void blocksGetClassLoader() {
            assertBlocked("sources.class.getClassLoader()");
        }
    }

    @Nested
    @DisplayName("File/Network (IO) must be blocked")
    class IOBlocked {
        @Test
        void blocksFile() {
            assertBlocked("new java.io.File('/etc/passwd').exists()");
        }

        @Test
        void blocksUrl() {
            assertBlocked("new java.net.URL('http://google.com').openStream()");
        }
    }

    @Nested
    @DisplayName("System/Runtime must be blocked")
    class SystemBlocked {
        @Test
        void blocksSystemExit() {
            assertBlocked("java.lang.System.exit(0)");
        }

        @Test
        void blocksRuntime() {
            assertBlocked("java.lang.Runtime.getRuntime().exec('ls')");
        }

        @Test
        void blocksSystemProperties() {
            assertBlocked("java.lang.System.getProperty('user.home')");
        }
    }
}
