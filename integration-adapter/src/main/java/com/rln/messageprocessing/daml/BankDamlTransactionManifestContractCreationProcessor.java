/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.daml;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.ContractId;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankDamlTransactionManifestContractCreationProcessor extends MessageProcessor<CreatedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BankDamlTransactionManifestContractCreationProcessor.class);
    private final TransactionManifestCache transactionManifestCache;

    public BankDamlTransactionManifestContractCreationProcessor(TransactionManifestCache transactionManifestCache) {
        logger.info("Created BankDamlTransactionManifestContractCreationProcessor");
        this.transactionManifestCache = transactionManifestCache;
    }

    @Override
    public void updateCache(CreatedEvent msg) {
        var transactionManifest = com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.fromValue(msg.getArguments());
        var contractId = new ContractId(msg.getContractId());
        logger.info("Updating Transaction Manifest cache with {}", transactionManifest.groupId);
        transactionManifestCache.write(transactionManifest.groupId, contractId);
    }
}
