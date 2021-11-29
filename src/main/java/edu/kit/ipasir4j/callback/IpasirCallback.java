package edu.kit.ipasir4j.callback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows a Java method to be used as a callback for ipasir.
 *
 * The annotated method must adhere to the signature imposed by the {@link #type() callback type}.
 * Each (valid) annotated method generates a dedicated class named as specified by {@link #target()} that can be used
 * by the {@link edu.kit.ipasir4j.Solver} class to make the method accessible from ipasir.
 *
 * @apiNote To use this annotation, ipasir4j must be added as an annotation processor.
 * @see edu.kit.ipasir4j.Solver#setLearn(SolverData, int, Class)
 * @see edu.kit.ipasir4j.Solver#setTerminate(SolverData, Class)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface IpasirCallback {

    Type type();

    String target() default "";

    enum Type {
        LEARN, TERMINATE
    }
}
