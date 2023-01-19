/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.daml.ledger.javaapi.data.ContractId;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveTransferProposalMarker;
import com.rln.damlCodegen.workflow.transferproposal.autoapprovetype.LimitedMaxAmount;
import com.rln.gui.backend.implementation.balanceManagement.AccountEventListener;
import com.rln.gui.backend.implementation.balanceManagement.AutoApproveEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceTestUtil;
import com.rln.gui.backend.implementation.profiles.GuiBackendTestProfile;
import com.rln.gui.backend.model.ApprovalProperties;
import com.rln.gui.backend.model.ApprovalProperties.ApprovalModeEnum;
import com.rln.gui.backend.model.LedgerAddressDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@TestProfile(GuiBackendTestProfile.class)
@QuarkusTest
class AutoapproveApiImplTest extends LedgerBaseTest {

  @Inject
  AutoApproveEventListener autoApproveEventListener;

  @Inject
  AccountEventListener accountEventListener;

  // TODO the corresponding endpoint seems to be deleted from the Swagger definition
//  @Test
//  void apiAutoapproveListGet() throws InvalidProtocolBufferException {
//    var createdMarker = publishLimitMarker(getCurrentBankPartyId(), USD, TRANSACTION_AMOUNT);
//    List<ApprovalProperties> result = RestAssured.given()
//        .when().get("/api/autoapprove/list")
//        .then()
//        .statusCode(200)
//        .extract().body().as(new TypeRef<>() {
//        });
//
//    Assertions.assertEquals(1, result.size());
//    var autoApproveResult = result.get(0);
//    // Assertions.assertEquals(getCurrentBankPartyId().getValue(), autoApproveResult.getUsername());
//    // TODO fix this
//    Assertions.assertEquals(USD, autoApproveResult.getAddress());
//    Assertions.assertEquals(TRANSACTION_AMOUNT, autoApproveResult.getLimit());
//
//    cleanupMarker(getCurrentBankPartyId(), createdMarker);
//  }

  @Test
  void apiAutoapprovePost() throws InvalidProtocolBufferException {
    RestAssured.given()
        .accept(ContentType.JSON)
        .contentType(ContentType.JSON)
        .body(new ApprovalProperties(SENDER_IBAN, ApprovalModeEnum.LIMIT, TRANSACTION_AMOUNT))
        .when().post("/api/approval/properties")
        .then()
        .statusCode(204);

    var autoApproveContractWithId= SANDBOX.getLedgerAdapter().getMatchedContract(
      getCurrentBankPartyId(),
        AutoApproveTransferProposalMarker.TEMPLATE_ID,
        ContractId::new);
    var autoApprove = AutoApproveTransferProposalMarker.fromValue(autoApproveContractWithId.record);
    Assertions.assertEquals(getCurrentBankPartyId().getValue(), autoApprove.owner);
    Assertions.assertEquals(SENDER_IBAN, autoApprove.address);
    Assertions.assertInstanceOf(LimitedMaxAmount.class, autoApprove.autoApproveType);
    LimitedMaxAmount limitedMaxAmount = (LimitedMaxAmount) autoApprove.autoApproveType;
    Assertions.assertEquals(TRANSACTION_AMOUNT, limitedMaxAmount.bigDecimalValue);

    cleanupMarker(getCurrentBankPartyId(), AutoApproveTransferProposalMarker.TEMPLATE_ID, autoApproveContractWithId.contractId);
  }

  @Test
  void apiAutoapprovePostUpdate() throws InvalidProtocolBufferException {
    publishLimitMarker(getCurrentBankPartyId(), SENDER_IBAN, TRANSACTION_AMOUNT);
    RestAssured.given()
        .accept(ContentType.JSON)
        .contentType(ContentType.JSON)
        .body(new ApprovalProperties(SENDER_IBAN, ApprovalModeEnum.LIMIT, TRANSACTION_AMOUNT_2))
        .when().post("/api/approval/properties")
        .then()
        .statusCode(204);

    var autoApproveContractWithId= SANDBOX.getLedgerAdapter().getMatchedContract(
        getCurrentBankPartyId(),
        AutoApproveTransferProposalMarker.TEMPLATE_ID,
        ContractId::new);
    var autoApprove = AutoApproveTransferProposalMarker.fromValue(autoApproveContractWithId.record);
    Assertions.assertEquals(getCurrentBankPartyId().getValue(), autoApprove.owner);
    Assertions.assertEquals(SENDER_IBAN, autoApprove.address);
    Assertions.assertInstanceOf(LimitedMaxAmount.class, autoApprove.autoApproveType);
    LimitedMaxAmount limitedMaxAmount = (LimitedMaxAmount) autoApprove.autoApproveType;
    Assertions.assertEquals(TRANSACTION_AMOUNT_2, limitedMaxAmount.bigDecimalValue);

    cleanupMarker(getCurrentBankPartyId(), AutoApproveTransferProposalMarker.TEMPLATE_ID, autoApproveContractWithId.contractId);
  }

  @Test
  void apiGetAddressSettingsListWhenLimit() throws InvalidProtocolBufferException {
    var liquidAmount = 500;
    var balance = BalanceTestUtil
        .populateBalance(liquidAmount, SENDER_IBAN, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);
    var balanceLimit = publishLimitMarker(getCurrentBankPartyId(), SENDER_IBAN, TRANSACTION_AMOUNT);

    List<LedgerAddressDTO> result = RestAssured.given()
        .when().get("/api/ledger/addresses")
        .then()
        .statusCode(200)
        .extract().body().as(new TypeRef<>() {
        });


    Assertions.assertEquals(1, result.size());

    var ledgerAddressInfo = result.get(0);

    Assertions.assertEquals(SENDER_IBAN, ledgerAddressInfo.getAddress());
    Assertions.assertEquals("LIMIT", ledgerAddressInfo.getApprovalMode());
    Assertions.assertEquals(TRANSACTION_AMOUNT.doubleValue(), ledgerAddressInfo.getApprovalLimit());
    Assertions.assertTrue(ledgerAddressInfo.getIsIBAN());
    // The following fields are just filled with dummy values
    Assertions.assertEquals(null, ledgerAddressInfo.getId());
    Assertions.assertEquals(0, ledgerAddressInfo.getClientId());
    Assertions.assertTrue(ledgerAddressInfo.getBearerToken().isEmpty());

    cleanupMarker(getCurrentBankPartyId(), Balance.TEMPLATE_ID, balance);
    cleanupMarker(getCurrentBankPartyId(), AutoApproveTransferProposalMarker.TEMPLATE_ID, balanceLimit);
  }

  @Test
  void apiGetAddressSettingsListWhenManual() throws InvalidProtocolBufferException {
    var liquidAmount = 500;
    var balance = BalanceTestUtil
        .populateBalance(liquidAmount, SENDER_IBAN, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

    List<LedgerAddressDTO> result = RestAssured.given()
        .when().get("/api/ledger/addresses")
        .then()
        .statusCode(200)
        .extract().body().as(new TypeRef<>() {
        });


    Assertions.assertEquals(1, result.size());

    var ledgerAddressInfo = result.get(0);

    Assertions.assertEquals(SENDER_IBAN, ledgerAddressInfo.getAddress());
    Assertions.assertEquals("MANUAL", ledgerAddressInfo.getApprovalMode());
    Assertions.assertEquals(null, ledgerAddressInfo.getApprovalLimit());
    Assertions.assertTrue(ledgerAddressInfo.getIsIBAN());
    // The following fields are just filled with dummy values
    Assertions.assertEquals(null, ledgerAddressInfo.getId());
    Assertions.assertEquals(0, ledgerAddressInfo.getClientId());
    Assertions.assertTrue(ledgerAddressInfo.getBearerToken().isEmpty());

    cleanupMarker(getCurrentBankPartyId(), Balance.TEMPLATE_ID, balance);
  }

  private ContractId publishLimitMarker(Party party, String address, BigDecimal amount)
      throws InvalidProtocolBufferException {
    var autoApproveMarker = new AutoApproveTransferProposalMarker(party.getValue(), Instant.now(), address, new LimitedMaxAmount(amount));
    SANDBOX.getLedgerAdapter().createContract(getCurrentBankPartyId().asParty().get(),
        AutoApproveTransferProposalMarker.TEMPLATE_ID, autoApproveMarker.toValue());
    return SANDBOX.getLedgerAdapter().getCreatedContractId(party, AutoApproveTransferProposalMarker.TEMPLATE_ID, ContractId::new);
  }

  private void cleanupMarker(Party partyId, Identifier identifier, ContractId contractId) throws InvalidProtocolBufferException {
    SANDBOX.getLedgerAdapter().exerciseChoice(partyId,
        new ExerciseCommand(identifier, contractId.getValue(), "Archive", new DamlRecord()));
  }
}
