/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.exercise;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.rln.client.damlClient.listeners.base.ExercisedEventDamlListener;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.common.Constants;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import com.rln.messageprocessing.MessageProcessor;

import java.util.List;

public class ApproveRejectProposalDamlListener extends ExercisedEventDamlListener {
    public ApproveRejectProposalDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber, MessageProcessor<ExercisedEvent> messageProcessor) {
        super(shardPartyIds, subscriber, messageProcessor, TransferProposal.TEMPLATE_ID, List.of(Constants.APPROVE_PROPOSAL_CHOICE, Constants.REJECT_PROPOSAL_CHOICE));
    }
}
