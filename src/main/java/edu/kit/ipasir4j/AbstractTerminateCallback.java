package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

public abstract class AbstractTerminateCallback<T extends SolverData>
        implements AbstractCallback<T>, SolverTerminateCallback {

    @Override
    public final int onTerminateQuestion(MemoryAddress dataAddr) {
        return onTerminateQuestion(dataFrom(dataAddr)) ? 1 : 0;
    }

    protected abstract boolean onTerminateQuestion(T data);
}
