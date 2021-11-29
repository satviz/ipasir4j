package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

public abstract class SolverData {

    protected final MemoryAddress address;

    protected SolverData(MemoryAddress address) {
        this.address = address;
    }

}
