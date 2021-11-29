package edu.kit.ipasir4j.callback;

import jdk.incubator.foreign.MemoryAddress;

import java.util.HashMap;
import java.util.Map;

public final class DataRegistry {

    private static final Map<MemoryAddress, SolverData> DATA_POINTERS = new HashMap<>();

    public static void put(SolverData data) {
        DATA_POINTERS.put(data.getAddress(), data);
    }

    public static SolverData get(MemoryAddress address) {
        return DATA_POINTERS.get(address);
    }

    public static SolverData remove(MemoryAddress address) {
        return DATA_POINTERS.remove(address);
    }

}
