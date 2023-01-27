/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.daml.ledger.javaapi.data.ContractId;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.client.damlClient.TestUtils;
import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.RejectedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil.Subject;
import com.rln.gui.backend.implementation.common.GuiBackendConstants;
import com.rln.gui.backend.implementation.profiles.GuiBackendTestProfile;
import com.rln.gui.backend.model.Transaction;
import com.rln.gui.backend.ods.TransferProposalManager;
import com.rln.gui.backend.test.util.Eventually;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@TestProfile(GuiBackendTestProfile.class)
@QuarkusTest
class TransactionsApiImplTest extends LedgerBaseTest {

  @InjectMock
  RandomShardPartyPicker randomShardPartyPicker;
  @Inject
  TransferProposalManager transferProposalManager;

  @Test
  void apiCreatePost() throws InvalidProtocolBufferException {
    Mockito.when(randomShardPartyPicker.pickRandomShardParty())
        .thenReturn(getSchedulerPartyId().getValue());
    RestAssured.given()
        .accept(ContentType.JSON)
        .contentType(ContentType.JSON)
        .body(TransactionsTestUtils.createTransferProposalRequest(GROUP_ID, MESSAGE_ID, PAYLOAD))
        .when().post("/transferProposal")
        .then()
        .statusCode(200);

    var initiateTransferContractWithId = SANDBOX.getLedgerAdapter().getMatchedContract(
        getCurrentBankPartyId(),
        InitiateTransfer.TEMPLATE_ID,
        ContractId::new);
    var initiateTransfer = InitiateTransfer.fromValue(initiateTransferContractWithId.record);
    TransactionsTestUtils.checkInitiateTransfer(initiateTransfer, GROUP_ID, getCurrentBankPartyId(),
        getSchedulerPartyId());

    cleanupContract(getCurrentBankPartyId(), InitiateTransfer.TEMPLATE_ID,
        initiateTransferContractWithId.contractId.getValue());
  }

  @Test
  void apiApprovalStatusPost() throws InvalidProtocolBufferException {
    var transferProposalCid = publishTransferProposalToLedger(
        getCurrentBankPartyId(), getSchedulerPartyId(), getAssemblerPartyId(),
        GROUP_ID, MESSAGE_ID, USD_INSTRUMENT_SETTLEMENT_STEP).getValue();

    RestAssured.given()
        .accept(ContentType.JSON)
        .contentType(ContentType.JSON)
        .body(TransactionsTestUtils.createApprovalRequest(transferProposalCid))
        .when().post("/api/approval")
        .then()
        .statusCode(200);

    var approvedCid = SANDBOX.getLedgerAdapter().getCreatedContractId(
        getCurrentBankPartyId(),
        ApprovedTransferProposal.TEMPLATE_ID,
        TransactionsTestUtils
            .createApprovedTransferProposalMatcher(getCurrentBankPartyId().getValue(),
                USD_INSTRUMENT_SETTLEMENT_STEP,
                MESSAGE_ID, GROUP_ID),
        ContractId::new);

    cleanupContract(getCurrentBankPartyId(), ApprovedTransferProposal.TEMPLATE_ID, approvedCid.getValue());
  }

  @Test
  void apiApprovalListGet() throws InvalidProtocolBufferException {
    var transferProposalCid = publishTransferProposalToLedger(
        getCurrentBankPartyId(), getSchedulerPartyId(), getAssemblerPartyId(),
        GROUP_ID, MESSAGE_ID, USD_INSTRUMENT_SETTLEMENT_STEP).getValue();

    Eventually.eventually(() -> {
      List<Transaction> result = RestAssured.given()
          .when().get("/api/approval/list")
          .then()
          .statusCode(200)
          .extract().body().as(new TypeRef<>() {
          });

      Assertions.assertEquals(2, result.size());

      TransactionsTestUtils
        .checkListedApprovalResult(transferProposalCid, result.get(0), GROUP_ID, MESSAGE_ID,
          SENDER_IBAN, Subject.SENDER, BANK_BIC, TRANSACTION_AMOUNT.negate());
      TransactionsTestUtils
        .checkListedApprovalResult(transferProposalCid, result.get(1), GROUP_ID, MESSAGE_ID,
          RECEIVER_IBAN, Subject.RECEIVER, BANK_BIC, TRANSACTION_AMOUNT);
    });
    cleanupContract(getSchedulerPartyId(), TransferProposal.TEMPLATE_ID, transferProposalCid);
  }

  @Test
  void apiTransactionsGetApproved() throws InvalidProtocolBufferException {
    var transferProposalCid = publishTransferProposalToLedger(
        getCurrentBankPartyId(), getSchedulerPartyId(), getAssemblerPartyId(),
        GROUP_ID, MESSAGE_ID, USD_INSTRUMENT_SETTLEMENT_STEP).getValue();
    TransactionsTestUtils
        .checkListedTransactionsForSenderReceiver(transferProposalCid, GROUP_ID, MESSAGE_ID,
            SENDER_IBAN, RECEIVER_IBAN,
            BANK_BIC, USD, TRANSACTION_AMOUNT, GuiBackendConstants.WAITING_STATUS);

    SANDBOX.getLedgerAdapter().exerciseChoice(
        getCurrentBankPartyId(),
        TestUtils.toExerciseCommand(new TransferProposal.ContractId(transferProposalCid)
            .exerciseApproveProposal(Optional.empty(), true)));
    var approvedCid = SANDBOX.getLedgerAdapter().getCreatedContractId(
        getCurrentBankPartyId(),
        ApprovedTransferProposal.TEMPLATE_ID,
        TransactionsTestUtils
            .createApprovedTransferProposalMatcher(getCurrentBankPartyId().getValue(),
                USD_INSTRUMENT_SETTLEMENT_STEP,
                MESSAGE_ID, GROUP_ID),
        ContractId::new).getValue();

    TransactionsTestUtils
        .checkListedTransactionsForSenderReceiver(approvedCid, GROUP_ID, MESSAGE_ID, SENDER_IBAN,
            RECEIVER_IBAN, BANK_BIC,
            USD, TRANSACTION_AMOUNT, GuiBackendConstants.SUCCESS_STATUS);

    cleanupContract(getCurrentBankPartyId(), ApprovedTransferProposal.TEMPLATE_ID, approvedCid);
  }

  @Test
  void apiTransactionsGetRejected() throws InvalidProtocolBufferException {
    var transferProposalCid = publishTransferProposalToLedger(
        getCurrentBankPartyId(), getSchedulerPartyId(), getAssemblerPartyId(),
        GROUP_ID, MESSAGE_ID, USD_INSTRUMENT_SETTLEMENT_STEP).getValue();

    TransactionsTestUtils
        .checkListedTransactionsForSenderReceiver(transferProposalCid, GROUP_ID, MESSAGE_ID,
            SENDER_IBAN, RECEIVER_IBAN,
            BANK_BIC, USD, TRANSACTION_AMOUNT, GuiBackendConstants.WAITING_STATUS);

    SANDBOX.getLedgerAdapter().exerciseChoice(
        getCurrentBankPartyId(),
        TestUtils.toExerciseCommand(new TransferProposal.ContractId(transferProposalCid)
            .exerciseRejectProposal(Optional.empty())));
    var rejectedCid = SANDBOX.getLedgerAdapter().getCreatedContractId(
        getCurrentBankPartyId(),
        RejectedTransferProposal.TEMPLATE_ID,
        TransactionsTestUtils
            .createRejectedTransferProposalMatcher(getCurrentBankPartyId().getValue(),
                USD_INSTRUMENT_SETTLEMENT_STEP,
                MESSAGE_ID, GROUP_ID),
        ContractId::new).getValue();

    TransactionsTestUtils
        .checkListedTransactionsForSenderReceiver(rejectedCid, GROUP_ID, MESSAGE_ID, SENDER_IBAN,
            RECEIVER_IBAN, BANK_BIC,
            USD, TRANSACTION_AMOUNT, GuiBackendConstants.REJECTED_STATUS);

    cleanupContract(getCurrentBankPartyId(), RejectedTransferProposal.TEMPLATE_ID, rejectedCid);
  }

}
