package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.implementation.config.SetlClient;
import com.rln.gui.backend.implementation.config.SetlParty;

import java.nio.file.Path;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class SetlPartySupplierUnitTest {

    static private final Long celloId = 1L;
    static private final String celloDamlParty = "Cello Bank::1220246d023e23ebb1a69f2b2d6b8b5f009a41496b72f69c9cf4a0437dde9a063541";
    static private final Path configFile = Path.of("src/test/resources/parties.json");
    static private final SetlPartySupplier setlPartySupplier = new SetlPartySupplier(configFile);
    static private final SetlParty celloSetlParty = SetlParty.builder()
            .id(celloId)
            .name("Cello Bank")
            .damlPartyId(celloDamlParty)
            .baseUrl("http://cello-participant:8080/api/")
            .clients(List.of(
                    SetlClient.builder()
                            .name("Alice")
                            .clientId(1L)
                            .iban("XYZ-IBAN")
                            .bearerToken("token-XYZ-IBAN")
                            .build()
            ))
            .build();

    @Test
    void testReadSetlParties() {
        MatcherAssert.assertThat(setlPartySupplier.getParties(), Matchers.hasSize(1));
    }

    @Test
    void getSetlParty() {
        MatcherAssert
                .assertThat(setlPartySupplier.getSetlParty(celloDamlParty), Matchers.is(celloSetlParty));
        MatcherAssert
                .assertThat(setlPartySupplier.getSetlParty("SomeOtherDamlParty"), Matchers.nullValue());
    }

    @Test
    void getSetlPartyId() {
        MatcherAssert
                .assertThat(setlPartySupplier.getSetlPartyId(celloDamlParty), Matchers.is(celloId));
        MatcherAssert
                .assertThat(setlPartySupplier.getSetlPartyId("SomeOtherDamlParty"), Matchers.nullValue());
    }
}