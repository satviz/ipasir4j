package edu.kit.ipasir4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SymbolLookup;

/**
 * Entrypoint for interfacing with Ipasir.
 *
 * <p><strong>IMPORTANT!</strong> Before this class can be used or even loaded in any way,
 * an ipasir implementation must be loaded using {@link System#load(String)} or
 * {@link System#loadLibrary(String)}.
 *
 * @see #init()
 */
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

  /**
   * Call ipasir_signature.
   *
   * @return the solver signature.
   */
  public static String signature() {
    try {
      return CLinker.toJavaString((MemoryAddress) SIGNATURE.invokeExact());
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  /**
   * Call ipasir_init.
   *
   * @return a {@link Solver} object encapsulating the solver pointer
   *         returned by the ipasir implementation.
   */
  public static Solver init() {
    try {
      return new Solver((MemoryAddress) INIT.invokeExact());
    } catch (Throwable e) {
      throw new IpasirInvocationException(e);
    }
  }

  static MethodHandle lookupFunction(
      String name, MethodType methodType, FunctionDescriptor descriptor
  ) {
    String fullName = PREFIX + name;
    var address = SymbolLookup.loaderLookup().lookup(fullName)
        .orElseThrow(() -> new IpasirNotFoundException(fullName));
    return CLinker.getInstance().downcallHandle(address, methodType, descriptor);
  }

}
