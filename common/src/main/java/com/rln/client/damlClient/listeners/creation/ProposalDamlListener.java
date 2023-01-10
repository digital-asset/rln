/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.creation;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.client.damlClient.listeners.base.CreatedEventDamlListener;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import com.rln.messageprocessing.MessageProcessor;

import java.util.List;

public class ProposalDamlListener extends CreatedEventDamlListener {

    public ProposalDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber, MessageProcessor<CreatedEvent> messageProcessor) {
        super(shardPartyIds, subscriber, messageProcessor, TransferProposal.TEMPLATE_ID);
    }
}
