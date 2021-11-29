package edu.kit.ipasir4j.callback;

import jdk.incubator.foreign.MemoryAddress;

public abstract class SolverData implements AutoCloseable {

    private final MemoryAddress address;

    protected SolverData(MemoryAddress address) {
        this.address = address;
    }

    public MemoryAddress getAddress() {
        return address;
    }

    @Override
    public void close() {
        DataRegistry.remove(address);
    }
}
