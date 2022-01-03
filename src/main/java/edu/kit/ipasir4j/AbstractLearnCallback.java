package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public abstract class AbstractLearnCallback<T extends SolverData> implements AbstractCallback<T>, SolverLearnCallback {

    @Override
    public final void onClauseLearn(MemoryAddress dataAddr, MemoryAddress clauseAddr) {
        int[] clause = MemorySegment.globalNativeSegment().asSlice(clauseAddr)
                .elements(CTypes.INT_32)
                .mapToInt(MemoryAccess::getInt)
                .takeWhile((i) -> i != 0)
                .toArray();
        T data = dataFrom(dataAddr);
        onSolverLearn(data, clause);
    }

    protected abstract void onSolverLearn(T data, int[] clause);
}
