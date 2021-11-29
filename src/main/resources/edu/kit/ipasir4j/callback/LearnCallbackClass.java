package %s;

import edu.kit.ipasir4j.CTypes;

import edu.kit.ipasir4j.callback.LearnCallbackMarker;
import edu.kit.ipasir4j.callback.DataRegistry;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

public final class %s implements LearnCallbackMarker {

    public static void callback(MemoryAddress $dataAddr$, MemoryAddress $clauseAddr$) {
        // This might not be performant enough in some use cases - need to run profiler
        int[] clause = MemorySegment.globalNativeSegment().asSlice($clauseAddr$)
                .elements(CTypes.INT_32)
                .mapToInt(MemoryAccess::getInt)
                .takeWhile((i) -> i != 0)
                .toArray();

        %s.%s((%s) DataRegistry.get($dataAddr$), clause);

    }

}