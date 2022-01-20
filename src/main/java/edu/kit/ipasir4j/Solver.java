package edu.kit.ipasir4j;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.ResourceScope;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.NoSuchElementException;

/**
 * A class representing an ipasir solver object.
 *
 * It gives access to all ipasir functions that can be called on a solver.
 *
 * <strong>IMPORTANT!</strong> Before this class can be used or even loaded in any way,
 * an ipasir implementation must be loaded using {@link System#load(String)} or
 * {@link System#loadLibrary(String)}.
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
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CTypes.INT_32));

    private static final MethodHandle ASSUME
            = Ipasir.lookupFunction("assume",
            MethodType.methodType(void.class, MemoryAddress.class, int.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CTypes.INT_32));

    private static final MethodHandle SOLVE
            = Ipasir.lookupFunction("solve",
            MethodType.methodType(int.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER));

    private static final MethodHandle VAL
            = Ipasir.lookupFunction("val",
            MethodType.methodType(int.class, MemoryAddress.class, int.class),
            FunctionDescriptor.of(CTypes.INT_32, CLinker.C_POINTER, CTypes.INT_32));

    private static final MethodHandle FAILED
            = Ipasir.lookupFunction("failed",
            MethodType.methodType(int.class, MemoryAddress.class, int.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CTypes.INT_32));

    private static final MethodHandle SET_TERMINATE
            = Ipasir.lookupFunction("set_terminate",
            MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER));

    private static final MethodHandle SET_LEARN
            = Ipasir.lookupFunction("set_learn",
            MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class, int.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_POINTER));

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
     * @param pointer The address of the underlying solver. The value at the address must be a valid solver.
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

    public void add(int litOrZero) {
        try {
            ADD.invokeExact(pointer, litOrZero);
        } catch (Throwable e) {
            throw new IpasirInvocationException(e);
        }
    }

    public void assume(int lit) {
        try {
            ASSUME.invokeExact(pointer, lit);
        } catch (Throwable e) {
            throw new IpasirInvocationException(e);
        }
    }

    public Result solve() {
        try {
            return Result.getByRepresentative((int) SOLVE.invokeExact(pointer));
        } catch (Throwable e) {
            throw new IpasirInvocationException(e);
        }
    }

    public int val(int lit) {
        try {
            return (int) VAL.invokeExact(pointer, lit);
        } catch (Throwable e) {
            throw new IpasirInvocationException(e);
        }
    }

    public boolean failed(int lit) {
        try {
            return (int) FAILED.invokeExact(pointer, lit) != 0;
        } catch (Throwable e) {
            throw new IpasirInvocationException(e);
        }
    }

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

    public void release() {
        try {
            RELEASE.invokeExact(pointer);
        } catch (Throwable e) {
            throw new IpasirInvocationException(e);
        }
    }

    @Override
    public void close() {
        release();
        terminateFunctionScope.close();
        learnFunctionScope.close();
    }

    public enum Result {
        INTERRUPTED(0),
        SATISFIABLE(10),
        UNSATISFIABLE(20);

        private final int repr;

        Result(int repr) {
            this.repr = repr;
        }

        public int getRepresentative() {
            return repr;
        }

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
