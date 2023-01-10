/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.producer;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.damlClient.RLNDamlClient;
import com.rln.gui.backend.implementation.config.DamlLedgerConfiguration;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class DamlClientProducer {
    Logger logger = LoggerFactory.getLogger(DamlClientProducer.class);

    @ApplicationScoped
    @Produces
    public RLNClient getDefaultRLNClient(DamlLedgerConfiguration damlLedgerConfiguration, GuiBackendConfiguration guiBackendConfiguration) {
        String host = damlLedgerConfiguration.host();
        int port = damlLedgerConfiguration.port();
        logger.info("Created RLNClient with host {} and port {}", host, port);
        return new RLNDamlClient(host, port, guiBackendConfiguration.numberOfLedgerSubmitterThreads(),
            guiBackendConfiguration.ledgerBatchSubmissionMaxMsec(), guiBackendConfiguration.ledgerBatchSubmissionMaxSize());
    }
}