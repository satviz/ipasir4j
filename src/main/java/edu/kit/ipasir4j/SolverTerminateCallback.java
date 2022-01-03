package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

public interface SolverTerminateCallback {

    int onTerminateQuestion(MemoryAddress data);

}
