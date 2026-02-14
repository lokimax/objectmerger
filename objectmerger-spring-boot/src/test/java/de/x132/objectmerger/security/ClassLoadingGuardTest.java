package de.x132.objectmerger.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ClassLoadingGuard Security Tests")
class ClassLoadingGuardTest {

  private ClassLoadingGuard guard;

  @BeforeEach
  void setUp() {
    guard = new ClassLoadingGuard();
    guard.setAllowedPackages(List.of("de.x132.objectmerger.model."));
  }

  @Nested
  @DisplayName("Allowed classes load successfully")
  class AllowedClassesLoadSuccessfully {

    @Test
    void allowsMapClass() {
      assertDoesNotThrow(
          () -> {
            Class<?> clazz = guard.loadClassSafely("java.util.Map");
            assertEquals(Map.class, clazz);
          });
    }

    @Test
    void allowsHashMapClass() {
      assertDoesNotThrow(
          () -> {
            Class<?> clazz = guard.loadClassSafely("java.util.HashMap");
            assertEquals(Map.class, clazz);
          });
    }

    @Test
    void allowsLinkedHashMapClass() {
      assertDoesNotThrow(
          () -> {
            Class<?> clazz = guard.loadClassSafely("java.util.LinkedHashMap");
            assertEquals(Map.class, clazz);
          });
    }

    @Test
    void allowsWhitelistedModelClass() {
      assertDoesNotThrow(() -> guard.loadClassSafely("de.x132.objectmerger.model.Person"));
    }
  }

  @Nested
  @DisplayName("Dangerous classes are blocked")
  class DangerousClassesAreBlocked {

    @Test
    void blocksRuntimeClass() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("java.lang.Runtime"));
    }

    @Test
    void blocksProcessBuilderClass() {
      assertThrows(
          SecurityException.class, () -> guard.loadClassSafely("java.lang.ProcessBuilder"));
    }

    @Test
    void blocksSystemClass() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("java.lang.System"));
    }

    @Test
    void blocksFileClass() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("java.io.File"));
    }

    @Test
    void blocksSocketClass() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("java.net.Socket"));
    }

    @Test
    void blocksNioFileClass() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("java.nio.file.Files"));
    }

    @Test
    void blocksReflectionClass() {
      assertThrows(
          SecurityException.class, () -> guard.loadClassSafely("java.lang.reflect.Method"));
    }

    @Test
    void blocksSunInternalClass() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("sun.misc.Unsafe"));
    }

    @Test
    void blocksComSunClass() {
      assertThrows(
          SecurityException.class,
          () -> guard.loadClassSafely("com.sun.net.httpserver.HttpServer"));
    }

    @Test
    void blocksJavaxClass() {
      assertThrows(
          SecurityException.class, () -> guard.loadClassSafely("javax.script.ScriptEngineManager"));
    }

    @Test
    void blocksConcurrentClass() {
      assertThrows(
          SecurityException.class, () -> guard.loadClassSafely("java.util.concurrent.Executors"));
    }
  }

  @Nested
  @DisplayName("Unlisted packages are blocked")
  class UnlistedPackagesAreBlocked {

    @Test
    void blocksArbitraryThirdPartyClass() {
      assertThrows(
          SecurityException.class, () -> guard.loadClassSafely("org.apache.commons.io.FileUtils"));
    }

    @Test
    void blocksClassOutsideWhitelist() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("com.example.SomeClass"));
    }
  }

  @Nested
  @DisplayName("Custom allowed packages work correctly")
  class CustomAllowedPackagesWork {

    @Test
    void allowsCustomPackageWhenConfigured() {
      guard.setAllowedPackages(List.of("de.x132.objectmerger.model.", "com.mycompany.dto."));

      assertThrows(SecurityException.class, () -> guard.loadClassSafely("com.example.SomeClass"));
    }
  }

  @Nested
  @DisplayName("Null and blank inputs are rejected")
  class NullAndBlankInputsAreRejected {

    @Test
    void blocksNullClassName() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely(null));
    }

    @Test
    void blocksBlankClassName() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely("   "));
    }

    @Test
    void blocksEmptyClassName() {
      assertThrows(SecurityException.class, () -> guard.loadClassSafely(""));
    }
  }
}
