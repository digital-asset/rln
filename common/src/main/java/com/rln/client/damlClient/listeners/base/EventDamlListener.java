/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.base;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.TransactionTree;
import com.daml.ledger.javaapi.data.TreeEvent;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public abstract class EventDamlListener<E extends TreeEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EventDamlListener.class);

    private final List<String> shardPartyIds;
    private final RLNLedgerSubscriber subscriber;
    private final MessageProcessor<E> messageProcessor;
    private final Set<Identifier> templateId;
    private final Class<E> eventType;
    private final Predicate<TreeEvent> predicate;

    public EventDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber,
                             MessageProcessor<E> messageProcessor,
                             Set<Identifier> templateIds,
                             Class<E> eventType,
                             Predicate<TreeEvent> predicate) {
        this.shardPartyIds = shardPartyIds;
        this.subscriber = subscriber;
        this.messageProcessor = messageProcessor;
        this.templateId = templateIds;
        this.eventType = eventType;
        this.predicate = predicate;
    }

    public void subscribe() {
        for (String shardPartyId : shardPartyIds) {
            subscriber.subscribe(shardPartyId, new TransactionConsumer());
        }
    }

    private class TransactionConsumer implements io.reactivex.functions.Consumer<TransactionTree> {

        @Override
        public void accept(TransactionTree transaction) {
            var filtered = transaction.getEventsById().values().stream()
                    .filter(e -> eventType.isAssignableFrom(e.getClass()) && templateId.contains(e.getTemplateId()) && predicate.test(e))
                    .map(e -> (E) e)
                    .collect(Collectors.toList());
            logger.info("Getting transactionTree (total: {}, filtered: {})", transaction.getEventsById().size(), filtered.size());
            logger.debug("Transaction: {}", filtered);
            filtered.forEach(messageProcessor);
        }
    }
}
