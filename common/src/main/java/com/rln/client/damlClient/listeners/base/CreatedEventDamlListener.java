/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.base;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Identifier;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.messageprocessing.MessageProcessor;

import java.util.List;
import java.util.Set;

public abstract class CreatedEventDamlListener extends EventDamlListener<CreatedEvent> {
    public CreatedEventDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber,
                                    MessageProcessor<CreatedEvent> messageProcessor, Identifier templateId) {
        super(shardPartyIds, subscriber, messageProcessor, Set.of(templateId), CreatedEvent.class, anything -> true);
    }

    public CreatedEventDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber,
                                    MessageProcessor<CreatedEvent> messageProcessor, Set<Identifier> templateIds) {
        super(shardPartyIds, subscriber, messageProcessor, templateIds, CreatedEvent.class, anything -> true);
    }
}
