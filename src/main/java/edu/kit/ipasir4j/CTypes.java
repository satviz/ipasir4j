package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryLayout;

import java.nio.ByteOrder;

final class CTypes {

  static final MemoryLayout INT_32 = MemoryLayout.valueLayout(4, ByteOrder.nativeOrder());

}
