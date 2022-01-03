package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

/**
 * An implementation of {@link SolverData} representing a null pointer, or "no data".
 */
public final class NullData extends SolverData {

    public static final NullData INSTANCE = new NullData();

    private NullData() {
        super(MemoryAddress.NULL);
    }
}
