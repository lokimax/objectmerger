package de.x132.objectmerger.exception;

/** Thrown when the MergeDefinition or FieldDefinition is invalid. */
public class ConfigurationException extends ObjectMergerException {
  public ConfigurationException(String message) {
    super(message);
  }

  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
