/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation;

import com.daml.ledger.javaapi.data.ContractId;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Party;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveTransferProposalMarker;
import com.rln.damlCodegen.workflow.transferproposal.autoapprovetype.LimitedMaxAmount;
import com.rln.gui.backend.implementation.GuiBackendTest;
import com.rln.gui.backend.implementation.LedgerBaseTest;
import com.rln.gui.backend.model.ApprovalProperties;
import com.rln.gui.backend.model.ApprovalProperties.ApprovalModeEnum;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@TestProfile(GuiBackendTest.class)
@QuarkusTest
class AutoapproveApiImplTest extends LedgerBaseTest {

  // TODO the corresponding endpoint seems to be deleted from the Swagger definition
//  @Test
//  void apiAutoapproveListGet() throws InvalidProtocolBufferException {
//    var createdMarker = publishMarker(getCurrentBankPartyId(), USD, TRANSACTION_AMOUNT);
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

    cleanupMarker(getCurrentBankPartyId(), autoApproveContractWithId.contractId);
  }

  @Test
  void apiAutoapprovePostUpdate() throws InvalidProtocolBufferException {
    publishMarker(getCurrentBankPartyId(), USD, TRANSACTION_AMOUNT);
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

    cleanupMarker(getCurrentBankPartyId(), autoApproveContractWithId.contractId);
  }

  private ContractId publishMarker(Party party, String label, BigDecimal amount)
      throws InvalidProtocolBufferException {
    var autoApproveMarker = new AutoApproveTransferProposalMarker(party.getValue(), Instant.now(), label, new LimitedMaxAmount(amount));
    SANDBOX.getLedgerAdapter().createContract(getCurrentBankPartyId().asParty().get(),
        AutoApproveTransferProposalMarker.TEMPLATE_ID, autoApproveMarker.toValue());
    return SANDBOX.getLedgerAdapter().getCreatedContractId(party, AutoApproveTransferProposalMarker.TEMPLATE_ID, ContractId::new);
  }

  private void cleanupMarker(Party partyId, ContractId contractId) throws InvalidProtocolBufferException {
    SANDBOX.getLedgerAdapter().exerciseChoice(partyId,
        new ExerciseCommand(AutoApproveTransferProposalMarker.TEMPLATE_ID, contractId.getValue(), "Archive", new DamlRecord()));
  }
}
