package edu.kit.ipasir4j;

public class IpasirInvocationException extends RuntimeException {

  public IpasirInvocationException(Throwable cause) {
    super("Error while invoking an ipasir function", cause);
  }

}
