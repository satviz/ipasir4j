package edu.kit.ipasir4j;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

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

    // callback handles for upcalls
    // TODO: 29/11/2021  

    private final MemoryAddress pointer;

    public Solver(MemoryAddress pointer) {
        this.pointer = pointer;
    }

    public void add(int litOrZero) {
        Ipasir.invokeIpasir(ADD, pointer, litOrZero);
    }

    public void assume(int lit) {
        Ipasir.invokeIpasir(ASSUME, pointer, lit);
    }

    public Result solve() {
        return Result.getByRepresentative((int) Ipasir.invokeIpasir(SOLVE, pointer));
    }

    public int val(int lit) {
        return (int) Ipasir.invokeIpasir(VAL, pointer, lit);
    }

    public boolean failed(int lit) {
        return (int) Ipasir.invokeIpasir(FAILED, pointer, lit) != 0;
    }

    public <T extends SolverData> void setTerminate(T data, Predicate<? super T> terminate) {
        // TODO: 29/11/2021
    }

    public <T extends SolverData> void setLearn(T data, int maxLength, BiConsumer<? super T, int[]> learn) {
        // TODO: 29/11/2021  
    }

    public void release() {
        Ipasir.invokeIpasir(RELEASE, pointer);
    }

    @Override
    public void close() {
        release();
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
