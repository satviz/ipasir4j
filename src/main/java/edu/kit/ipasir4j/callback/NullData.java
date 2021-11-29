package edu.kit.ipasir4j.callback;

import jdk.incubator.foreign.MemoryAddress;

public final class NullData extends SolverData {

    public static final NullData INSTANCE = new NullData();

    private NullData() {
        super(MemoryAddress.NULL);
    }
}
