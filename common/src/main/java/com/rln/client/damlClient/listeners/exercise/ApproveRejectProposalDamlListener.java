/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.exercise;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.rln.client.damlClient.listeners.base.CreatedEventDamlListener;
import com.rln.client.damlClient.listeners.base.ExercisedEventDamlListener;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.common.Constants;
import com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.RejectedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import com.rln.messageprocessing.MessageProcessor;

import java.util.List;
import java.util.Set;

public class ApproveRejectProposalDamlListener extends CreatedEventDamlListener {
    public ApproveRejectProposalDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber, MessageProcessor<CreatedEvent> messageProcessor) {
        super(shardPartyIds, subscriber, messageProcessor, Set.of(ApprovedTransferProposal.TEMPLATE_ID, RejectedTransferProposal.TEMPLATE_ID));
    }
}
