package com.rln.gui.backend.implementation;

import com.rln.gui.backend.implementation.methods.LedgerBaseTest;
import com.rln.gui.backend.implementation.profiles.GuiBackendTestProfile;
import com.rln.gui.backend.model.PartyDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@TestProfile(GuiBackendTestProfile.class)
@QuarkusTest
class PartyApiImplTest extends LedgerBaseTest {

  @Test
  void getMyParty() {
    var result = RestAssured
      .get("/api/parties/me")
      .then()
      .statusCode(200)
      .extract().body().as(PartyDTO.class);

    var expected = new PartyDTO(BASEURL, BANK_BIC, PARTY_ID, PARTY_NAME);
    MatcherAssert.assertThat(result, Matchers.is(expected));
  }
}