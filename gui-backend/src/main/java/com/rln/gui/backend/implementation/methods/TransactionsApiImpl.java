/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.model.TransactionStatusUpdate;
import com.rln.client.damlClient.ApproveRejectProposalChoiceParameters;
import com.rln.client.damlClient.RLNClient;
import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal.ContractId;
import com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil;
import com.rln.gui.backend.implementation.common.GuiBackendConstants;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.implementation.converter.TransferProposalToApiTypeConverter;
import com.rln.gui.backend.model.Approval;
import com.rln.gui.backend.model.ApprovalProperties;
import com.rln.gui.backend.model.Finalised;
import com.rln.gui.backend.model.Transaction;
import com.rln.gui.backend.model.TransactionStatusUpdate.StatusEnum;
import com.rln.gui.backend.model.TransferProposal;
import com.rln.gui.backend.ods.TransferProposalRepository;

import java.util.Collections;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionsApiImpl {
  // GUI Backend is run for a Daml participant, it communicates directly with the ledger, so it settles on the ledger
  private final boolean GUI_BACKEND_SETTLES_ON_LEDGER = true;
  private final RandomShardPartyPicker schedulerRandomShardPartyPicker;
  private final GuiBackendConfiguration guiBackendConfiguration;
  private final TransferProposalRepository transferProposals;
  private final RLNClient rlnClient;
  private final TransferProposalToApiTypeConverter converter;

  public TransactionsApiImpl(RandomShardPartyPicker schedulerRandomShardPartyPicker,
                             GuiBackendConfiguration guiBackendConfiguration,
                             TransferProposalToApiTypeConverter converter,
                             TransferProposalRepository transferProposals,
                             RLNClient rlnClient) {
    this.schedulerRandomShardPartyPicker = schedulerRandomShardPartyPicker;
    this.guiBackendConfiguration = guiBackendConfiguration;
    this.transferProposals = transferProposals;
    this.rlnClient = rlnClient;
    this.converter = converter;
  }

  public Object updateTransactionApprovalStatus(
      @Valid TransactionStatusUpdate transactionStatusUpdate) {
    var approved = transactionStatusUpdate.getStatus().equals(StatusEnum.APPROVE);
    var contractId = CompoundUniqueIdUtil.parseSubjectAndContractId(transactionStatusUpdate.getId())._2;
    ApproveRejectProposalChoiceParameters parameters =
        new ApproveRejectProposalChoiceParameters(
            guiBackendConfiguration.partyId(),
            new ContractId(contractId),
            approved,
            GuiBackendConstants.DEFAULT_GUI_BACKEND_REASON,
            GUI_BACKEND_SETTLES_ON_LEDGER);
    rlnClient.exerciseApproveRejectProposalChoice(transactionStatusUpdate.getId(), parameters);
    return Collections.emptyMap();
  }

  public List<Transaction> getRequiredApprovals() {
    return transferProposals
        .findAllWaiting().stream()
        .map(converter::transferProposalToTransaction)
        .collect(Collectors.toList());
  }

  public List<Transaction> getTransactions(Boolean incompleteOnly, String address, Long limit,
      Long offset) {
    // TODO: Decide if parameters are required and if not how to handle their absence.
    var actualLimit = Objects.requireNonNullElse(limit, Long.MAX_VALUE);
    var actualOffset = Objects.requireNonNullElse(offset, 0L);
    return transferProposals
        .findAll(actualLimit, actualOffset).stream()
        .map(converter::transferProposalToTransaction)
        .collect(Collectors.toList());
  }

  public void updateApprovalProperties(@Valid ApprovalProperties approvalProperties) {

  }

  public void finalised(@Valid Finalised finalised) {

  }

  public List<Approval> transferProposal(@Valid TransferProposal transferProposal) {
    rlnClient.createInitiateTransferContract(translateToInitiateTransfer(transferProposal));
    return Collections.emptyList();
  }

  private InitiateTransfer translateToInitiateTransfer(TransferProposal createTransaction) {
    var payload = createTransaction.getPayload();
    var initiator = guiBackendConfiguration.partyId();
    var groupId = createTransaction.getGroupId();
    var scheduler = schedulerRandomShardPartyPicker.pickRandomShardParty();
    return new InitiateTransfer(groupId, initiator, scheduler, payload);
  }
}
