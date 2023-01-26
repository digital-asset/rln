package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.implementation.config.SetlParty;
import com.rln.gui.backend.implementation.profiles.GuiBackendTestProfile;
import com.rln.gui.backend.model.PartyDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import javax.inject.Inject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

@TestProfile(GuiBackendTestProfile.class)
@QuarkusTest
class PartyApiImplTest extends LedgerBaseTest {

  @InjectMock
  SetlPartySupplier setlPartySupplier;

  @Inject
  PartyApiImpl partyApi;

  @Test
  void getMyParty() {
    PartyDTO expected = new PartyDTO(LedgerBaseTest.BASEURL, List.of(LedgerBaseTest.BANK_BIC), LedgerBaseTest.PARTY_ID, LedgerBaseTest.PARTY_NAME);

    PartyDTO result = RestAssured.given()
        .when().get("/api/parties/me")
        .then()
        .statusCode(200)
        .extract().body().as(new TypeRef<>() {
        });

    MatcherAssert.assertThat(result, Matchers.is(expected));
  }

  @Test
  void getParties() {
    BDDMockito.given(setlPartySupplier.getParties()).willReturn(List.of(new SetlParty(BASEURL, PARTY_ID, getCurrentBankPartyId().getValue(), PARTY_NAME)));

    List<PartyDTO> result = RestAssured.given()
        .when().get("/api/parties")
        .then()
        .statusCode(200)
        .extract().body().as(new TypeRef<>() {
        });

    MatcherAssert.assertThat(result, Matchers.not(Matchers.empty()));
  }
}