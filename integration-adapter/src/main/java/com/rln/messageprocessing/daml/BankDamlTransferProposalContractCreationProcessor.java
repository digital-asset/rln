/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.daml;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.PartyCache.BankPartyCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.TransferProposal;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.conversion.daml2kafka.TransferProposalContractCreationToKafka;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal.ContractId;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankDamlTransferProposalContractCreationProcessor extends MessageProcessor<CreatedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BankDamlTransferProposalContractCreationProcessor.class);

    private final KafkaSubmitter<TransferProposal> kafkaSubmitter;
    private final TransferProposalContractCreationToKafka conversion;
    private final BankPartyCache bankPartyCache;
    private final TransferProposalCache transferProposalCache;
    private final PartyManager partyManager;


    public BankDamlTransferProposalContractCreationProcessor(KafkaSubmitter<TransferProposal> kafkaSubmitter,
                                                             TransferProposalContractCreationToKafka conversion,
                                                             BankPartyCache bankPartyCache,
                                                             TransferProposalCache transferProposalCache, PartyManager partyManager) {
        logger.info("Created BankDamlTransferProposalContractCreationProcessor with translation {}", conversion);
        this.kafkaSubmitter = kafkaSubmitter;
        this.conversion = conversion;
        this.bankPartyCache = bankPartyCache;
        this.transferProposalCache = transferProposalCache;
        this.partyManager = partyManager;
    }

    @Override
    protected void updateCache(CreatedEvent input) {
        var transferProposal = com.rln.damlCodegen.workflow.transferproposal.TransferProposal.fromValue(input.getArguments());
        String ownerPartyId = transferProposal.owner;

        if (!partyManager.hasPartyId(ownerPartyId)) {
            logger.info("Owner party of the TransferProposal {} does not belong to current entity, ignored as we are just witnessing", ownerPartyId);
            return;
        }

        var ownerBic = partyManager.getBic(ownerPartyId);
        var transferProposalKey = new TransferProposalKey(transferProposal.groupId, transferProposal.messageId, ownerBic);
        logger.info("Updating cache (Bankparty Cache, TransferProposal Cache) with {}", transferProposalKey);
        bankPartyCache.write(transferProposalKey, ownerPartyId);
        var contractId = new ContractId(input.getContractId());
        transferProposalCache.write(transferProposalKey, contractId);
    }

    @Override
    protected void publish(CreatedEvent input) {
        var transferProposal = com.rln.damlCodegen.workflow.transferproposal.TransferProposal.fromValue(input.getArguments());
        String ownerPartyId = transferProposal.owner;

        if (!partyManager.hasPartyId(ownerPartyId)) {
            logger.info("Owner party of the TransferProposal {} does not belong to current entity, not sending proposal as we are just witnessing", ownerPartyId);
            return;
        }

        var converted = conversion.createdEventToKafka(input);
        logger.info("Publishing to Kafka, GroupId: {}", converted.getGroupId());
        kafkaSubmitter.submit(converted);
        logger.debug("Published to Kafka, GroupId: {}, Message: {}", converted.getGroupId(), converted);
    }
}
