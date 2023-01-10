/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.daml;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal.ContractId;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblerDamlTransferProposalContractCreationProcessor extends MessageProcessor<CreatedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AssemblerDamlTransferProposalContractCreationProcessor.class);
    private final TransferProposalCache transferProposalCache;
    private final PartyManager partyManager;

    public AssemblerDamlTransferProposalContractCreationProcessor(TransferProposalCache transferProposalCache, PartyManager partyManager) {
        logger.info("Created AssemblerDamlTransferProposalContractCreationProcessor");
        this.transferProposalCache = transferProposalCache;
        this.partyManager = partyManager;
    }

    @Override
    public void updateCache(CreatedEvent msg) {
        var transferProposal = com.rln.damlCodegen.workflow.transferproposal.TransferProposal.fromValue(msg.getArguments());
        var ownerBic = partyManager.getBic(transferProposal.owner);
        var transferProposalKey = new TransferProposalKey(transferProposal.groupId, transferProposal.messageId, ownerBic);
        var contractId = new ContractId(msg.getContractId());
        logger.info("Updating TransferProposal cache with {}", transferProposalKey);
        transferProposalCache.write(transferProposalKey, contractId);
    }
}
