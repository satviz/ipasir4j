package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BiConsumer;

public final class LearnConsumer<T extends SolverData> {

    private final T data;
    private final BiConsumer<T, int[]> consumer;

    LearnConsumer(T data, BiConsumer<T, int[]> consumer) {
        this.data = data;
        this.consumer = consumer;
    }

    public void callback(MemoryAddress dataPointer, MemoryAddress clausePointer) {
        // This might not be performant enough in some use cases - need to run profiler
        int[] clause = MemorySegment.globalNativeSegment().asSlice(clausePointer)
                .elements(CTypes.INT_32)
                .mapToInt(MemoryAccess::getInt)
                .takeWhile((i) -> i != 0)
                .toArray();

        consumer.accept(data, clause);
    }
}
