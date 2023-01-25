package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.implementation.GuiBackendApiImplementation;
import com.rln.gui.backend.implementation.profiles.GuiBackendTestProfile;
import com.rln.gui.backend.model.PartyDTO;
import com.rln.gui.backend.model.WalletDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import javax.inject.Inject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@TestProfile(GuiBackendTestProfile.class)
@QuarkusTest
class WalletsApiImplTest extends LedgerBaseTest {
  @Test
  void getWalletsReturnsStaticSingleWallet() {
    List<WalletDTO> result = RestAssured.get("/api/wallets")
        .then()
        .statusCode(200)
        .extract().body().as(new TypeRef<>(){});

    Assertions.assertIterableEquals(result, List.of(GuiBackendApiImplementation.ONLY_SUPPORTED_WALLET));
  }
}