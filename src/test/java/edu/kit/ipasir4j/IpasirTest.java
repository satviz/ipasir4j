package edu.kit.ipasir4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IpasirTest {

  @BeforeAll
  static void setUpLibrary() throws IOException {
    SharedLibUtil.setUpLibrary();
  }

  @AfterAll
  static void deleteLibrary() throws IOException {
    SharedLibUtil.deleteLibrary();
  }

  @Test
  void testSignature() {
    assertEquals("cadical-1.5.2", Ipasir.signature());
  }

  @Test
  void testInit() {
    var solver = Ipasir.init();
    assertNotNull(solver);
  }

}
