/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.Party;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.RejectedTransferProposal;
import com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil;
import com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil.Subject;
import com.rln.gui.backend.implementation.common.GuiBackendConstants;
import com.rln.gui.backend.model.Transaction;
import com.rln.gui.backend.model.TransactionStatusUpdate.StatusEnum;
import com.rln.gui.backend.test.util.Eventually;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

public class TransactionsTestUtils {

  public static Map<String, Object> createTransferProposalRequest(String groupId, String messageId, String payload) {
    Map<String, Object> transferProposalRequest = new HashMap<>();
    transferProposalRequest.put("groupId", groupId);
    transferProposalRequest.put("messageId", messageId);
    transferProposalRequest.put("payload", payload);
    return transferProposalRequest;
  }

  private Map<String, Object> getUpdateApprovalRequest(String messageId) {
    Map<String, Object> updateApprovalRequest = new HashMap<>();
    updateApprovalRequest.put("id", messageId);
    updateApprovalRequest.put("status", "APPROVE");
    return updateApprovalRequest;
  }

  public static Map<String, Object> createApprovalRequest(Subject subject, String contractId) {
    Map<String, Object> updateApprovalRequest = new HashMap<>();
    updateApprovalRequest.put("id", CompoundUniqueIdUtil.getCompoundUniqueId(subject, contractId));
    updateApprovalRequest.put("status", StatusEnum.APPROVE);
    return updateApprovalRequest;
  }

  public static void checkListedTransactionsForSenderReceiver(String contractId, String groupId, String messageId, String senderIban,
                                                              String receiverIban, String bic, String assetCode,
                                                              BigDecimal amount, String status) {
    Eventually.eventually(() -> {
      List<Transaction> result = RestAssured.given()
        .when().get("/api/transactions")
        .then()
        .statusCode(200)
        .extract().body().as(new TypeRef<>() {
        });
      Assertions.assertEquals(2, result.size());

      checkListedTransactionResult(contractId, result.get(0), groupId, messageId, senderIban, Subject.SENDER, status, bic, assetCode, amount.negate());
      checkListedTransactionResult(contractId, result.get(1), groupId, messageId, receiverIban, Subject.RECEIVER, status, bic, assetCode, amount);
    });
  }

  public static void checkListedTransactionResult(String contractId, Transaction listedItem, String groupId, String messageId,
                                                  String iban, Subject subject, String status, String bic, String assetCode,
                                                  BigDecimal transactionAmount) {
    Assertions.assertEquals(CompoundUniqueIdUtil.getCompoundUniqueId(subject, contractId), listedItem.getId());
    Assertions.assertEquals(groupId, listedItem.getTransactionId());
    Assertions.assertEquals(groupId, listedItem.getGroupId());
    Assertions.assertEquals(messageId, listedItem.getMessageId());
    Assertions.assertEquals(iban, listedItem.getAddress());
    Assertions.assertEquals(bic, listedItem.getPartyCode());
    Assertions.assertEquals(status, listedItem.getStatus());
    Assertions.assertEquals(0, transactionAmount.compareTo(listedItem.getBalance()));
    Assertions.assertEquals(assetCode, listedItem.getAssetCode());
  }

  public static void checkListedApprovalResult(String contractId, Transaction listedItem, String groupId, String messageId,
                                               String iban, Subject subject, String bic, BigDecimal transactionAmount) {
    Assertions.assertEquals(CompoundUniqueIdUtil.getCompoundUniqueId(subject, contractId), listedItem.getId());
    Assertions.assertEquals(groupId, listedItem.getTransactionId());
    Assertions.assertEquals(groupId, listedItem.getGroupId());
    Assertions.assertEquals(messageId, listedItem.getMessageId());
    Assertions.assertEquals(iban, listedItem.getAddress());
    Assertions.assertEquals(bic, listedItem.getPartyCode());
    Assertions.assertEquals(GuiBackendConstants.WAITING_STATUS, listedItem.getStatus());
    Assertions.assertEquals(0, transactionAmount.compareTo(listedItem.getBalance()));
  }

  public static void checkInitiateTransfer(InitiateTransfer initiateTransfer, String groupId, Party initiator, Party scheduler) {
    Assertions.assertEquals(groupId, initiateTransfer.groupId);
    Assertions.assertEquals(initiator.getValue(), initiateTransfer.initiator);
    Assertions.assertEquals(scheduler.getValue(), initiateTransfer.scheduler);
    Assert.assertFalse(initiateTransfer.payload.isBlank());
  }

  public static DamlRecord createApprovedTransferProposalMatcher(String bank, String assembler, String reason,
                                                                 SettlementStep step, String messageId,
                                                                 String groupId) {
    return new ApprovedTransferProposal(bank, assembler, Instant.EPOCH, Instant.EPOCH, Optional.ofNullable(reason), step,
            List.of(bank), LedgerBaseTest.PAYLOAD, messageId, groupId).toValue();

  }

  public static DamlRecord createRejectedTransferProposalMatcher(String bank, String assembler, String reason,
                                                                 SettlementStep step, String messageId,
                                                                 String groupId) {
    return new RejectedTransferProposal(bank, assembler, Instant.EPOCH, Instant.EPOCH, Optional.ofNullable(reason), step,
            List.of(bank), LedgerBaseTest.PAYLOAD, messageId, groupId).toValue();
  }
}
