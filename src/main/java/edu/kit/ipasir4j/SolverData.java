package edu.kit.ipasir4j;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;

/**
 * A {@code SolverData} object is the Java representation of a {@code data} pointer that is passed
 * to {@link Solver#setTerminate(MemoryAddress, SolverTerminateCallback) ipasir_set_terminate} or
 * {@link Solver#setLearn(MemoryAddress, int, SolverLearnCallback) ipasir_set_learn}. This pointer
 * can be used to gain access to additional solver-specific data.
 *
 * <p>The definition of the structure of this data and a corresponding Java API is delegated to
 * implementations of this class.
 *
 * <p>Instances of this class should be treated as resources that must be closed.
 * What {@link #close()} does is, again, implementation-defined.
 */
public abstract class SolverData implements AutoCloseable {

  private final MemoryAddress address;

  /**
   * Encapsulate the given address in this object.
   *
   * @param address The pointer to the actual solver data.
   */
  protected SolverData(MemoryAddress address) {
    this.address = address;
  }

  /**
   * Gets the address of the underlying data pointer.
   *
   * @return Return the address in memory of the data this object represents.
   */
  public MemoryAddress getAddress() {
    return address;
  }

  /**
   * Closes this data object by freeing the memory at the underlying data pointer.
   *
   * <p>This is only the default behaviour and may be overwritten by implementations.
   */
  @Override
  public void close() {
    CLinker.freeMemory(address);
  }
}
