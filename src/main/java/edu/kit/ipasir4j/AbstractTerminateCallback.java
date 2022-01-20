package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;

/**
 * An abstraction on top of {@link SolverTerminateCallback}.<br>
 * It transforms the data passed to it to a more convenient Java representation
 * and enables you to return a {@code boolean} instead of an {@code int}.
 *
 * @param <T> The type of {@link SolverData} this callback should receive.
 */
public abstract class AbstractTerminateCallback<T extends SolverData>
    implements AbstractCallback<T>, SolverTerminateCallback {

  @Override
  public final int onTerminateQuestion(MemoryAddress dataAddr) {
    return onTerminateQuestion(dataFrom(dataAddr)) ? 1 : 0;
  }

  /**
   * Called when the solver asks whether it should terminate the solving process.
   *
   * @param data The {@link SolverData} object.
   * @return {@code true} if it should be terminated, {@code false} if not.
   */
  protected abstract boolean onTerminateQuestion(T data);
}
