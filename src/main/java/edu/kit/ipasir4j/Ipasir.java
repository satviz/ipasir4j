package edu.kit.ipasir4j;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SymbolLookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public final class Ipasir {

    private static final String PREFIX = "ipasir_";

    private static final MethodHandle SIGNATURE
            = lookupFunction("signature",
            MethodType.methodType(MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_POINTER));

    private static final MethodHandle INIT
            = lookupFunction("init",
            MethodType.methodType(MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_POINTER));

    private Ipasir() {

    }

    public static String getSignature() {
        return CLinker.toJavaString((MemoryAddress) invokeIpasir(SIGNATURE));
    }

    public static Solver init() {
        return new Solver((MemoryAddress) invokeIpasir(INIT));
    }

    static MethodHandle lookupFunction(String name, MethodType methodType, FunctionDescriptor descriptor) {
        String fullName = PREFIX + name;
        var address = SymbolLookup.loaderLookup().lookup(fullName)
                .orElseThrow(() -> new IpasirNotFoundException(fullName));
        return CLinker.getInstance().downcallHandle(address, methodType, descriptor);
    }

    static Object invokeIpasir(MethodHandle handle, Object... args) {
        try {
            return handle.invokeExact(args);
        } catch (Throwable e) {
            throw new RuntimeException("Error while invoking an ipasir function (" + handle + ")", e);
        }
    }

}
