package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

public interface AbstractCallback<T extends SolverData> {

    T dataFrom(MemoryAddress dataAddr);

}
