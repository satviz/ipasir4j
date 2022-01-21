package edu.kit.ipasir4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class SharedLibUtil {

  private static Path libPath;

  static void setUpLibrary() throws IOException {
    try (var sharedLib = IpasirTest.class.getResourceAsStream("/libcadical.so")) {
      libPath = Files.createTempFile("libcadical", ".so");
      Files.copy(sharedLib, libPath, StandardCopyOption.REPLACE_EXISTING);
      System.load(libPath.toAbsolutePath().toString());
    }
  }

  static void deleteLibrary() throws IOException {
    Files.delete(libPath);
  }

}
