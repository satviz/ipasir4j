package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

import java.util.function.Predicate;

public final class TerminatePredicate<T extends SolverData> {

    private final T data;
    private final Predicate<T> predicate;

    TerminatePredicate(T data, Predicate<T> predicate) {
        this.data = data;
        this.predicate = predicate;
    }

    public int callback(MemoryAddress dataPointer) {
        return predicate.test(this.data) ? 1 : 0;
    }
}
