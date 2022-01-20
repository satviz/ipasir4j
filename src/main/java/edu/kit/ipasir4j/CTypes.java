package edu.kit.ipasir4j;

import java.nio.ByteOrder;
import jdk.incubator.foreign.MemoryLayout;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
final class CTypes {

  static final MemoryLayout INT_32 = MemoryLayout.valueLayout(4, ByteOrder.nativeOrder());

  private CTypes() {

  }
}
