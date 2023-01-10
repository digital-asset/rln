/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Event;
import com.daml.ledger.javaapi.data.GetActiveContractsResponse;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.LedgerOffset;
import com.daml.ledger.javaapi.data.Transaction;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.javaapi.data.TransactionTree;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

import java.util.Set;

public interface RLNClient {

    void createInitiateTransferContract(InitiateTransfer pac008);

    void createOrUpdateAutoApproveMarker(AutoApproveParameters autoApproveParameters);

    void exerciseCreateProposalsChoice(String groupId, CreateProposalsChoiceParameters createProposalChoiceParameters);

    void exerciseApproveRejectProposalChoice(String groupId, ApproveRejectProposalChoiceParameters acceptRejectChoiceParameters);

    void exerciseFinalizeRejectSettlement(String groupId, FinalizeRejectSettlementChoiceParameters finalizeRejectSettlementChoiceParameters);

    Flowable<Transaction> getTransactions(LedgerOffset offset, TransactionFilter filter);

    Flowable<TransactionTree> getTransactionTrees(String subscriberParty, LedgerOffset offset);

    Flowable<CreatedEvent> getActiveContracts(TransactionFilter filter);

    Flowable<GetActiveContractsResponse> getActiveContractsResponse(TransactionFilter filter);

    void subscribeForContinuousEvent(String subscriberParty, Set<Identifier> templatesIncluded, Consumer<Event> consumer);
}
