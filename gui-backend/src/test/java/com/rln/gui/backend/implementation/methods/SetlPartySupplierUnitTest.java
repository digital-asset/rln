package com.rln.gui.backend.implementation.methods;

import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class SetlPartySupplierUnitTest {
  static private final String celloDamlParty = "Cello Bank::1220246d023e23ebb1a69f2b2d6b8b5f009a41496b72f69c9cf4a0437dde9a063541";
  static private final Path configFile = Path.of("src/test/resources/parties.json");
  static private final SetlPartySupplier setlPartySupplier = new SetlPartySupplier(configFile);

  @Test
  void testReadSetlParties() {
    MatcherAssert.assertThat(setlPartySupplier.getParties(), Matchers.hasSize(1));
  }

  @Test
  void getSetlId() {
    MatcherAssert.assertThat(setlPartySupplier.getSetlId(celloDamlParty), Matchers.is(1L));
    MatcherAssert.assertThat(setlPartySupplier.getSetlId("SomeOtherDamlParty"), Matchers.nullValue());
  }
}