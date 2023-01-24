package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

class PartyApiImplUnitTest {
  @Test
  void testReadSetlParties() {
    var configuration = Mockito.mock(GuiBackendConfiguration.class);
    BDDMockito.given(configuration.partiesConfig()).willReturn(Path.of("src/test/resources/parties.json"));
    var partyApi = new PartyApiImpl(configuration, null);

    var result = partyApi.readSetlParties();

    MatcherAssert.assertThat(result, Matchers.hasSize(1));
  }
}