package edu.kit.ipasir4j;

/**
 * An exception thrown to indicate that an ipasir function could not be found.
 */
public class IpasirNotFoundException extends RuntimeException {

  /**
   * Constructs an exception with a custom message.
   *
   * @param functionName The name of the function that could not be found.
   */
  public IpasirNotFoundException(String functionName) {
    super("Failed to bind to ipasir function " + functionName
        + " - did you forget to load an ipasir implementation?");
  }
}
