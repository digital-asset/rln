/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.subscription;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.LedgerOffset;
import com.daml.ledger.javaapi.data.TransactionTree;
import com.rln.client.damlClient.RLNClient;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class RLNLedgerSubscriber implements LedgerSubscriber<Disposable> {

    private final RLNClient ledger;
    private final Scheduler scheduler;

    public RLNLedgerSubscriber(RLNClient ledger, Scheduler scheduler) {
        this.ledger = ledger;
        this.scheduler = scheduler;
    }

    @Override
    public Disposable subscribe(String subscribeParty, Consumer<TransactionTree> consumer) {
        var transactions = ledger.getTransactionTrees(
                subscribeParty, LedgerOffset.LedgerEnd.getInstance());

        // all transactions visible by the party will be passed to consumer(processor for filtering)
        return transactions
            .observeOn(scheduler)
            .forEach(consumer);
    }
}
