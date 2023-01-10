/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.creation;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.client.damlClient.listeners.base.CreatedEventDamlListener;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InitiationDamlListener extends CreatedEventDamlListener {
    private static final Logger logger = LoggerFactory.getLogger(InitiationDamlListener.class);

    public InitiationDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber, MessageProcessor<CreatedEvent> messageProcessor) {
        super(shardPartyIds, subscriber, messageProcessor, InitiateTransfer.TEMPLATE_ID);
        logger.info("Created InitiationDamlListener with ShardPartyId: {}, TemplateId: {}", shardPartyIds, InitiateTransfer.TEMPLATE_ID);
    }
}
