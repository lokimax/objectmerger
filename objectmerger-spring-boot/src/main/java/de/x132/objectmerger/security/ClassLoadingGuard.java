package de.x132.objectmerger.security;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "objectmerger.security")
public class ClassLoadingGuard {

  private static final List<String> DEFAULT_ALLOWED_PACKAGES =
      List.of("de.x132.objectmerger.model.");

  private List<String> allowedPackages = DEFAULT_ALLOWED_PACKAGES;

  public void setAllowedPackages(List<String> allowedPackages) {
    this.allowedPackages = allowedPackages;
  }

  public List<String> getAllowedPackages() {
    return allowedPackages;
  }

  public Class<?> loadClassSafely(String className) throws ClassNotFoundException {
    if (className == null || className.isBlank()) {
      throw new SecurityException("Class name must not be null or blank");
    }

    if (isMapClass(className)) {
      return Map.class;
    }

    rejectDangerousPackages(className);
    rejectUnlistedPackage(className);

    return Class.forName(className);
  }

  private boolean isMapClass(String className) {
    return "java.util.Map".equals(className)
        || "java.util.HashMap".equals(className)
        || "java.util.LinkedHashMap".equals(className);
  }

  private void rejectDangerousPackages(String className) {
    if (className.startsWith("java.lang.")
        || className.startsWith("java.io.")
        || className.startsWith("java.net.")
        || className.startsWith("java.nio.")
        || className.startsWith("javax.")
        || className.startsWith("sun.")
        || className.startsWith("com.sun.")
        || className.startsWith("java.lang.reflect.")
        || className.startsWith("java.util.concurrent.")) {
      log.warn("Blocked attempt to load dangerous class: {}", className);
      throw new SecurityException(
          "Loading class '" + className + "' is not allowed for security reasons");
    }
  }

  private void rejectUnlistedPackage(String className) {
    for (String allowedPackage : allowedPackages) {
      if (className.startsWith(allowedPackage)) {
        return;
      }
    }
    log.warn(
        "Blocked attempt to load class '{}' - not in allowed packages: {}",
        className,
        allowedPackages);
    throw new SecurityException(
        "Class '"
            + className
            + "' is not in the list of allowed packages. "
            + "Configure 'objectmerger.security.allowed-packages' to allow it.");
  }
}
