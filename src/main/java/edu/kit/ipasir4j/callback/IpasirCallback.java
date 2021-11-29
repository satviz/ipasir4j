package edu.kit.ipasir4j.callback;

public @interface IpasirCallback {

    Type type();

    String target() default "";

    enum Type {
        LEARN, TERMINATE
    }
}
