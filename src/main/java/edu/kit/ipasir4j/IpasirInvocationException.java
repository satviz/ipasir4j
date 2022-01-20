package edu.kit.ipasir4j;

/**
 * Thrown when a call to an ipasir function fails.
 */
public class IpasirInvocationException extends RuntimeException {

  /**
   * Like {@link Exception#Exception(Throwable)}, but with a custom exception message.
   *
   * @param cause The cause of this exception.
   */
  public IpasirInvocationException(Throwable cause) {
    super("Error while invoking an ipasir function", cause);
  }

}
