package de.x132.objectmerger.exception;

/**
 * Base exception for all ObjectMerger library exceptions. Extends RuntimeException to act as an
 * unchecked exception.
 */
public class ObjectMergerException extends RuntimeException {
  public ObjectMergerException(String message) {
    super(message);
  }

  public ObjectMergerException(String message, Throwable cause) {
    super(message, cause);
  }
}
