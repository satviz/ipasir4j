package edu.kit.ipasir4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.NoSuchElementException;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;

/**
 * A class representing an ipasir solver object.<br>
 * It gives access to all ipasir functions that can be called on a solver.
 *
 * <p>Objects of this class must be explicitly closed to release all associated resources. It is
 * recommended to call {@link #close()} or use objects of this class in a {@code try-with-resources}
 * statement.
 *
 * <p>The API of this class mostly delegates to the ipasir API only. For proper explanations of the
 * functions provided here, you should read
 * <a href="https://github.com/biotomas/ipasir/blob/master/ipasir.h">the ipasir documentation</a>.
 *
 * <p><strong>IMPORTANT!</strong> Before this class can be used or even loaded in any way,
 * an ipasir implementation must be loaded using {@link System#load(String)} or
 * {@link System#loadLibrary(String)}.
 *
 * @see Ipasir#init()
 */
public final class Solver implements AutoCloseable {

  // ipasir functions
  private static final MethodHandle RELEASE
      = Ipasir.lookupFunction("release",
      MethodType.methodType(void.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER));

  private static final MethodHandle ADD
      = Ipasir.lookupFunction("add",
      MethodType.methodType(void.class, MemoryAddress.class, int.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT));

  private static final MethodHandle ASSUME
      = Ipasir.lookupFunction("assume",
      MethodType.methodType(void.class, MemoryAddress.class, int.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT));

  private static final MethodHandle SOLVE
      = Ipasir.lookupFunction("solve",
      MethodType.methodType(int.class, MemoryAddress.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER));

  private static final MethodHandle VAL
      = Ipasir.lookupFunction("val",
      MethodType.methodType(int.class, MemoryAddress.class, int.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT));

  private static final MethodHandle FAILED
      = Ipasir.lookupFunction("failed",
      MethodType.methodType(int.class, MemoryAddress.class, int.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT));

  private static final MethodHandle SET_TERMINATE
      = Ipasir.lookupFunction("set_terminate",
      MethodType.methodType(void.class, MemoryAddress.class,
          MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER));

  private static final MethodHandle SET_LEARN
      = Ipasir.lookupFunction("set_learn",
      MethodType.methodType(void.class, MemoryAddress.class,
          MemoryAddress.class, int.class, MemoryAddress.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER,
          CLinker.C_INT, CLinker.C_POINTER));

  // handles for upcalls
  private static final MethodHandle TERMINATE_UPCALL_HANDLE
      = findVirtual(SolverTerminateCallback.class,
      "onTerminateQuestion",
      MethodType.methodType(int.class, MemoryAddress.class));

  private static final MethodHandle LEARN_UPCALL_HANDLE
      = findVirtual(SolverLearnCallback.class,
      "onClauseLearn",
      MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class));

  // pointer to the solver object
  private final MemoryAddress pointer;

  // scopes used to manage callback function pointers
  private ResourceScope terminateFunctionScope;
  private ResourceScope learnFunctionScope;

  /**
   * Create a new solver based on the given solver pointer.
   *
   * @param pointer The address of the underlying solver.
   *                The value at the address must be a valid ipasir solver.
   * @see Ipasir#init()
   */
  public Solver(MemoryAddress pointer) {
    this.pointer = pointer;
  }

  private static MethodHandle findVirtual(Class<?> c, String name, MethodType type) {
    try {
      return MethodHandles.publicLookup().findVirtual(c, name, type);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError("Could not find virtual method", e);
    }
  }

  /**
   * Call {@code ipasir_add} on this solver.
   *
   * @param litOrZero A variable literal (-n or +n) or 0 to mark the end of the clause.
   */
  public void add(int litOrZero) {
    try {
      ADD.invokeExact(pointer, litOrZero);
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Call {@code ipasir_assume} in this solver.
   *
   * @param lit A variable literal.
   */
  public void assume(int lit) {
    try {
      ASSUME.invokeExact(pointer, lit);
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Call {@code ipasir_solve} on this solver.
   *
   * @return A {@link Result} object that represents the result of the solving process.
   */
  public Result solve() {
    try {
      return Result.getByRepresentative((int) SOLVE.invokeExact(pointer));
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Call {@code ipasir_val} on this solver.
   *
   * @param lit The literal to obtain a value for.
   * @return The truth value of the literal after solving.
   */
  public int val(int lit) {
    try {
      return (int) VAL.invokeExact(pointer, lit);
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Call {@code ipasir_failed} on this solver.
   *
   * @param lit The literal for which to check whether it was used to prove unsatisfiability.
   * @return {@code true} for 1, {@code false} for 0.
   */
  public boolean failed(int lit) {
    try {
      return (int) FAILED.invokeExact(pointer, lit) != 0;
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Call {@code ipasir_set_terminate} on this solver.
   *
   * @param data A pointer to solver data that will be passed to the callback on each call.
   * @param callback The terminate callback.
   * @see AbstractTerminateCallback
   */
  public void setTerminate(MemoryAddress data, SolverTerminateCallback callback) {
    overrideTerminateScope();
    var callbackPointer = CLinker.getInstance().upcallStub(
        TERMINATE_UPCALL_HANDLE.bindTo(callback),
        FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER),
        terminateFunctionScope
    );
    try {
      SET_TERMINATE.invokeExact(pointer, data, callbackPointer);
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Call {@code ipasir_set_learn} on this solver.
   *
   * @param data A pointer to solver data that will be passed to the callback on each call.
   * @param maxLength The maximum length of clauses passed to the callback.
   * @param callback The learn callback.
   */
  public void setLearn(MemoryAddress data, int maxLength, SolverLearnCallback callback) {
    overrideLearnScope();
    var callbackPointer = CLinker.getInstance().upcallStub(
        LEARN_UPCALL_HANDLE.bindTo(callback),
        FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER),
        learnFunctionScope
    );
    try {
      SET_LEARN.invokeExact(pointer, data, maxLength, callbackPointer);
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  private void overrideTerminateScope() {
    if (terminateFunctionScope != null) {
      terminateFunctionScope.close();
    }
    terminateFunctionScope = ResourceScope.newConfinedScope();
  }

  private void overrideLearnScope() {
    if (learnFunctionScope != null) {
      learnFunctionScope.close();
    }
    learnFunctionScope = ResourceScope.newConfinedScope();
  }

  /**
   * Call {@code ipasir_release} on this solver.
   *
   * @see #close()
   */
  public void release() {
    try {
      RELEASE.invokeExact(pointer);
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Frees the resources associated with this solver, including the solver itself and possibly
   * callback function pointers.<br>
   * The solver cannot be used anymore after a call to {@code close()}.
   */
  @Override
  public void close() {
    release();
    if (terminateFunctionScope != null) {
      terminateFunctionScope.close();
    }
    if (learnFunctionScope != null) {
      learnFunctionScope.close();
    }
  }

  /**
   * A Java representation of the possible ipasir solver results.
   */
  public enum Result {
    /**
     * When the solver was interrupted. Corresponds to result value 0 in ipasir.
     */
    INTERRUPTED(0),

    /**
     * When the formula was found to be satisfiable. Corresponds to result value 10 in ipasir.
     */
    SATISFIABLE(10),

    /**
     * When the formula was found to be unsatisfiable. Corresponds to result value 20 in ipasir.
     */
    UNSATISFIABLE(20);

    private final int repr;

    Result(int repr) {
      this.repr = repr;
    }

    /**
     * Returns the ipasir result value corresponding to this result.
     *
     * @return 0, 10 or 20.
     */
    public int getRepresentative() {
      return repr;
    }

    /**
     * Gets the {@code Result} enum constant corresponding to the given ipasir result value.
     *
     * @param repr An ipasir result value: 0, 10 or 20.
     * @return The {@code Result} constant
     * @throws NoSuchElementException If the input is not a valid ipasir result value.
     */
    public static Result getByRepresentative(int repr) {
      return switch (repr) {
        case 0 -> INTERRUPTED;
        case 10 -> SATISFIABLE;
        case 20 -> UNSATISFIABLE;
        default -> throw new NoSuchElementException("Unknown representative " + repr);
      };
    }
  }

}
