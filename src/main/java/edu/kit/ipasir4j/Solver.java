package edu.kit.ipasir4j;

import edu.kit.ipasir4j.callback.DataRegistry;
import edu.kit.ipasir4j.callback.LearnCallbackMarker;
import edu.kit.ipasir4j.callback.SolverData;
import edu.kit.ipasir4j.callback.TerminateCallbackMarker;
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
 * @apiNote <strong>IMPORTANT!</strong> Before this class can be used or even loaded in any way,
 * an ipasir implementation must be loaded using {@link System#load(String)} or {@link System#loadLibrary(String)}.
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

    // callback types for upcalls
    private static final MethodType TERMINATE_UPCALL_TYPE
            = MethodType.methodType(int.class, MemoryAddress.class);

    private static final MethodType LEARN_UPCALL_TYPE
            = MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class);

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

    public void setTerminate(MemoryAddress data, MethodHandle callback) {
        overrideTerminateScope();
        var callbackPointer = CLinker.getInstance().upcallStub(
                callback, FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER), terminateFunctionScope);
        Ipasir.invokeIpasir(SET_TERMINATE, pointer, data, callbackPointer);
    }

    public void setLearn(MemoryAddress data, int maxLength, MethodHandle callback) {
        overrideLearnScope();
        var callbackPointer = CLinker.getInstance().upcallStub(
                callback, FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER), learnFunctionScope);
        Ipasir.invokeIpasir(SET_LEARN, pointer, data, maxLength, callbackPointer);
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

    public <T extends TerminateCallbackMarker, D extends SolverData> void setTerminate(
            D data, Class<T> callback
    ) {
        DataRegistry.put(data);
        setTerminate(data.getAddress(), findCallbackHandle(callback, TERMINATE_UPCALL_TYPE));
    }

    public <T extends LearnCallbackMarker, D extends SolverData> void setLearn(
            D data, int maxLength, Class<T> callback
    ) {
        DataRegistry.put(data);
        setLearn(data.getAddress(), maxLength, findCallbackHandle(callback, LEARN_UPCALL_TYPE));
    }

    private MethodHandle findCallbackHandle(Class<?> callbackType, MethodType type) {
        try {
            return MethodHandles.publicLookup().findStatic(callbackType, "callback", type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(callbackType
                    + " is not a proper callback type. Use @IpasirCallback to generate one.", e);
        }
    }

    // these two functions could be realised with runtime class generation. Maybe some time in the future.
    /*public <T extends SolverData> void setTerminate(T data, Predicate<? super T> terminate) {

    }

    public <T extends SolverData> void setLearn(T data, int maxLength, BiConsumer<? super T, int[]> learn) {

    }*/

    public void release() {
        Ipasir.invokeIpasir(RELEASE, pointer);
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
