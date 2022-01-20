package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

/**
 * A low-level interface representing a learn callback function for ipasir.
 */
@FunctionalInterface
public interface SolverLearnCallback {

  /**
   * Called when a solver learns a new clause.
   *
   * @param data The {@code data} pointer initially passed to
   *             {@link Solver#setLearn(MemoryAddress, int, SolverLearnCallback)}
   * @param clause The address of the beginning of a 0-terminated memory segment of literals (int).
   */
  void onClauseLearn(MemoryAddress data, MemoryAddress clause);

}
