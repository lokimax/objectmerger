package de.x132.objectmerger.exception;

/** Thrown when the input sources are invalid or incompatible. */
public class InvalidSourceException extends ObjectMergerException {
  public InvalidSourceException(String message) {
    super(message);
  }

  public InvalidSourceException(String message, Throwable cause) {
    super(message, cause);
  }
}
