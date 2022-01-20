package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

/**
 * An abstraction on top of {@link SolverLearnCallback}.<br>
 * It transforms the data passed to it to a more convenient Java representation.
 *
 * @param <T> The type of {@link SolverData} this callback should receive.
 */
public abstract class AbstractLearnCallback<T extends SolverData>
    implements AbstractCallback<T>, SolverLearnCallback {

  @Override
  public final void onClauseLearn(MemoryAddress dataAddr, MemoryAddress clauseAddr) {
    int[] clause = MemorySegment.globalNativeSegment().asSlice(clauseAddr)
        .elements(CTypes.INT_32)
        .mapToInt(MemoryAccess::getInt)
        .takeWhile(i -> i != 0)
        .toArray();
    T data = dataFrom(dataAddr);
    onClauseLearn(data, clause);
  }

  /**
   * Called when a solver learns a clause.
   *
   * @param data The {@link SolverData} object.
   * @param clause The clause that was learned, represented as an array of literals.
   */
  protected abstract void onClauseLearn(T data, int[] clause);
}
