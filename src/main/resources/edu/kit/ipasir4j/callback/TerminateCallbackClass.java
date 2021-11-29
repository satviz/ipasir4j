package %s;

import edu.kit.ipasir4j.callback.DataRegistry;
import edu.kit.ipasir4j.callback.TerminateCallbackMarker;

import jdk.incubator.foreign.MemoryAddress;

public final class %s implements TerminateCallbackMarker {

    public static int callback(MemoryAddress $dataAddr$) {
        return %s.%s((%s) DataRegistry.get($dataAddr$)) ? 1 : 0;
    }

}
