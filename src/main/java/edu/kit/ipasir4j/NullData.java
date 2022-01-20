package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

/**
 * An implementation of {@link SolverData} representing a null pointer, or "no data".
 *
 * @see #INSTANCE
 */
public final class NullData extends SolverData {

  /**
   * The singleton instance of this class.
   */
  public static final NullData INSTANCE = new NullData();

  private NullData() {
    super(MemoryAddress.NULL);
  }
}
