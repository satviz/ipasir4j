package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

public interface SolverLearnCallback {

  void onClauseLearn(MemoryAddress data, MemoryAddress clause);

}
