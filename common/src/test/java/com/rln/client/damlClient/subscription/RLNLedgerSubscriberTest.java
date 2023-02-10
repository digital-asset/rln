/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.subscription;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.TransactionTree;
import com.daml.ledger.javaapi.data.TreeEvent;
import com.rln.client.damlClient.RLNClient;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.schedulers.ImmediateThinScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Map;

class RLNLedgerSubscriberTest {
    protected static final Identifier DUMMY_IDENTIFIER = new Identifier("", "", "");
    protected static final String DAML_EVENT_ID = "DAML_EVENT_ID";
    protected static final String INITIATOR_PARTY_ID = "INITIATOR_PARTY_ID";

    @Test
    void GIVEN_transactions_WHEN_subscribe_THEN_consume_transaction() throws Exception {
        RLNClient rlnClient = Mockito.mock(RLNClient.class);
        TransactionTree transactionTree = Mockito.mock(TransactionTree.class);
        var event = Mockito.mock(CreatedEvent.class);
        Mockito.when(event.getTemplateId()).thenReturn(DUMMY_IDENTIFIER);
        Map<String, TreeEvent> eventsById = Map.of(DAML_EVENT_ID, event);
        Mockito.when(transactionTree.getEventsById()).thenReturn(eventsById);
        Flowable<TransactionTree> transactionStream = Flowable.just(transactionTree, transactionTree);
        Mockito.when(rlnClient.getTransactionTrees(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(transactionStream);

        RLNLedgerSubscriber subscriber = new RLNLedgerSubscriber(rlnClient, ImmediateThinScheduler.INSTANCE);
        Consumer<TransactionTree> consumer = Mockito.spy((new Consumer<TransactionTree>() {
            @Override
            public void accept(TransactionTree t) {
                // Do nothing.
                // We need to keep the `new Consumer..` form because using lambda
                // makes the test fail (spy does not like it)
            }
        }));
        subscriber.subscribe(INITIATOR_PARTY_ID, consumer);

        Mockito.verify(consumer, Mockito.times(2)).accept(transactionTree);
    }
}
