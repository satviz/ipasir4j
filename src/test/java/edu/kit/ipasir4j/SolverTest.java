package edu.kit.ipasir4j;

import jdk.incubator.foreign.MemoryAddress;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SolverTest {

  private Solver solver;

  @BeforeAll
  static void setUpLibrary() throws IOException {
    SharedLibUtil.setUpLibrary();
  }

  @AfterAll
  static void deleteLibrary() throws IOException {
    SharedLibUtil.deleteLibrary();
  }

  @BeforeEach
  void setUp() {
    solver = Ipasir.init();
  }

  @AfterEach
  void tearDown() {
    solver.close();
    solver = null;
  }

  @Test
  void testRun_satisfiable() {
    IntStream.of(1, 2, 3, 0, 1, 2, -3, 0, -1, 3, 0, -2, -3, 0).forEach(solver::add);
    solver.setLearn(MemoryAddress.NULL, 10, (data, clause) -> System.out.println("CLAUSE"));
    var result = solver.solve();
    assertEquals(Solver.Result.SATISFIABLE, result);
    var assignments = IntStream.range(1, 6)
        .map(solver::val)
        .map(x -> (int) Math.signum(x))
        .toArray();
    assertArrayEquals(new int[] {1, -1, 1, -1, -1}, assignments);
  }

  @Test
  void testRun_unsatisfiable() {
    IntStream.of(1, 2, 0, -1, 0, -2, 3, 0, -3, 0).forEach(solver::add);
    var result = solver.solve();
    assertEquals(Solver.Result.UNSATISFIABLE, result);
  }
}
