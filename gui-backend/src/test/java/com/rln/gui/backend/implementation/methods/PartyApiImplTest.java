package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.implementation.config.SetlClient;
import com.rln.gui.backend.implementation.config.SetlParty;
import com.rln.gui.backend.implementation.config.SetlTreasuryAccount;
import com.rln.gui.backend.implementation.profiles.GuiBackendTestProfile;
import com.rln.gui.backend.model.ClientDTO;
import com.rln.gui.backend.model.PartyDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import javax.inject.Inject;
import java.util.List;

@TestProfile(GuiBackendTestProfile.class)
@QuarkusTest
class PartyApiImplTest extends LedgerBaseTest {

    @InjectMock
    SetlPartySupplier setlPartySupplier;

    @Inject
    PartyApiImpl partyApi;

    @Test
    void getMyParty() {
        PartyDTO expected = new PartyDTO(LedgerBaseTest.BASEURL, List.of(LedgerBaseTest.BANK_BIC),
                LedgerBaseTest.PARTY_ID, LedgerBaseTest.PARTY_NAME);

        PartyDTO result = RestAssured
                .get("/api/parties/me")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<>() {
                });

        MatcherAssert.assertThat(result, Matchers.is(expected));
    }

    @Test
    void getParties() {
        BDDMockito.given(setlPartySupplier.getParties()).willReturn(List.of(
                new SetlParty(BASEURL,
                        PARTY_ID,
                        getCurrentBankPartyId().getValue(),
                        PARTY_NAME,
                        List.of(),
                        List.of())
        ));

        List<PartyDTO> result = RestAssured
                .get("/api/parties")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<>() {
                });

        MatcherAssert.assertThat(result, Matchers.not(Matchers.empty()));
    }

    @Test
    void getClients() {
        var client = new SetlClient(
                CLIENT_ID,
                CLIENT_NAME,
                SENDER_IBAN,
                "token-" + SENDER_IBAN
        );
        var treasury = new SetlTreasuryAccount(
                2,
                PARTY_NAME,
                SENDER_IBAN,
                "token-" + SENDER_IBAN
        );
        BDDMockito.given(setlPartySupplier.getSetlPartyByDamlParty(getCurrentBankPartyId().getValue()))
                .willReturn(new SetlParty(
                        BASEURL,
                        PARTY_ID,
                        getCurrentBankPartyId().getValue(),
                        PARTY_NAME,
                        List.of(client),
                        List.of()
                ));

        BDDMockito.given(setlPartySupplier.getTreasuryAccountsByProviderParty(getCurrentBankPartyId().getValue()))
                .willReturn(List.of(treasury));

        List<ClientDTO> result = RestAssured
                .get("/api/ledger/clients")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<>() {
                });

        ClientDTO expectedClient = new ClientDTO(client.getClientId(), client.getName());
        ClientDTO expectedTreasury = new ClientDTO(treasury.getClientId(), treasury.getIban());
        MatcherAssert.assertThat(result, Matchers.hasItem(expectedClient));
        MatcherAssert.assertThat(result, Matchers.hasItem(expectedTreasury));
    }
}