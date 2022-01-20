package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

/**
 * A superinterface for abstractions on top of the lower-level ipasir callback interfaces.
 *
 * @param <T> The type of {@link SolverData} this class uses.
 */
public interface AbstractCallback<T extends SolverData> {

  /**
   * Constructs a {@link SolverData} instance from an ipasir solver data address.
   *
   * @param dataAddr A data pointer like those passed to
   *                 {@link Solver#setTerminate(MemoryAddress, SolverTerminateCallback)}
   * @return A matching {@link SolverData} instance (or derivative).
   */
  T dataFrom(MemoryAddress dataAddr);

}
