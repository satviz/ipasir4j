package edu.kit.ipasir4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import jdk.incubator.foreign.CLinker;
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
    MemorySegment clauseSegment = MemorySegment.globalNativeSegment().asSlice(clauseAddr);
    int length = 0;
    long intSize = CLinker.C_INT.byteSize();
    while (MemoryAccess.getIntAtOffset(clauseSegment, length) != 0) {
      length += intSize;
    }
    int[] clause = clauseSegment.asSlice(0, length)
        .elements(CLinker.C_INT)
        .mapToInt(MemoryAccess::getInt)
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
