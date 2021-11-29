package edu.kit.ipasir4j;

public class IpasirNotFoundException extends RuntimeException {

    public IpasirNotFoundException(String functionName) {
        super("Failed to bind to ipasir function " + functionName
                + " - did you forget to load an ipasir implementation?");
    }
}
