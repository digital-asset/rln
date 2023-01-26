package com.rln.gui.backend.implementation.methods;

import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class SetlPartySupplierUnitTest {
  static private final Path configFile = Path.of("src/test/resources/parties.json");
  static private final SetlPartySupplier setlPartySupplier = new SetlPartySupplier(configFile);

  @Test
  void testReadSetlParties() {
    MatcherAssert.assertThat(setlPartySupplier.getParties(), Matchers.hasSize(1));
  }
}