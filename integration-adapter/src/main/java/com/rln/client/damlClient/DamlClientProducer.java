/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.configuration.AdapterConfiguration;
import com.rln.configuration.DamlLedgerConfiguration;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.Executors;

public class DamlClientProducer {
    Logger logger = LoggerFactory.getLogger(DamlClientProducer.class);

    @ApplicationScoped
    @Produces
    public RLNClient getDefaultRLNClient(DamlLedgerConfiguration damlLedgerConfiguration, AdapterConfiguration adapterConfiguration) {
        String host = damlLedgerConfiguration.host();
        int port = damlLedgerConfiguration.port();
        logger.info("Created RLNClient with host {} and port {}", host, port);
        return new RLNDamlClient(host, port, adapterConfiguration.numberOfLedgerSubmitterThreads(),
            adapterConfiguration.ledgerBatchSubmissionMaxMsec(), adapterConfiguration.ledgerBatchSubmissionMaxSize());
    }

    @ApplicationScoped
    @Produces
    public RLNLedgerSubscriber getRLNLedgerSubscriber(AdapterConfiguration adapterConfiguration, RLNClient client) {
        logger.info("Created RLNLedgerSubscriber with RLNClient {}, number of ledger subscriber threads: {}", client, adapterConfiguration.numberOfLedgerSubscriberThreads());
        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(adapterConfiguration.numberOfLedgerSubscriberThreads()));
        return new RLNLedgerSubscriber(client, scheduler);
    }
}
