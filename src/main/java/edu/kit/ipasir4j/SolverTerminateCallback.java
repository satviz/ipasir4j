package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

/**
 * A low-level interface representing a terminate callback function for ipasir.
 */
@FunctionalInterface
public interface SolverTerminateCallback {

  /**
   * Called by ipasir to check whether the solving process should be terminated.
   *
   * @param data The {@code data} pointer initially passed to
   *             {@link Solver#setTerminate(MemoryAddress, SolverTerminateCallback)}
   * @return a "boolean int" (1 for true, 0 for false)
   */
  int onTerminateQuestion(MemoryAddress data);

}
