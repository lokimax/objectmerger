package de.x132.objectmerger.exception;

/**
 * Thrown when an error occurs during the execution of a merge operation (e.g. reflection failure,
 * instantiation error).
 */
public class MergeExecutionException extends ObjectMergerException {
  public MergeExecutionException(String message) {
    super(message);
  }

  public MergeExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
