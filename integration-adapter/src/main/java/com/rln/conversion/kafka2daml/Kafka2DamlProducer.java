/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.kafka2daml;

import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.PartyCache.AssemblerPartyCache;
import com.rln.cache.PartyCache.BankPartyCache;
import com.rln.cache.PartyCache.SchedulerPartyCache;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.configuration.AdapterConfiguration;
import com.rln.configuration.AdapterMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * The idea is to have 1-1 translations from kafka to daml for different modes, like:
 * bank mode:
 * 1.translation of simplified initiation message from Kafka (which we will need to turn into a Daml contract)
 * 2.Reject/Approve message from kafka -> Reject/Approve choice exercise on daml
 * scheduler mode: translation of the "enriched" pacs008 message with settlement chain from kafka -> enriched pac008 in daml
 * assembler mode: finalized message from kafka -> finalize choice in daml
 */
public class Kafka2DamlProducer {

    Logger logger = LoggerFactory.getLogger(Kafka2DamlProducer.class);

    @ApplicationScoped
    @Produces
    public InitiationPacs008SwiftToDamlTranslation getInitiationTranslation(AdapterConfiguration adapterConfiguration,
                                                                            PartyManager partyManager,
                                                                            RandomShardPartyPicker schedulerRandomShardPartyPicker) {
        if (adapterConfiguration.mode() == AdapterMode.BANK) {
            logger.info("created InitiationSwiftToDamlTranslation under BANK mode");
            return new InitiationPacs008SwiftToDamlTranslation(partyManager, schedulerRandomShardPartyPicker);
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    public EnrichedPacs008SwiftToDamlTranslation getInitiationTranslation(AdapterConfiguration adapterConfiguration,
                                                                          PartyManager partyManager,
                                                                          RandomShardPartyPicker assemblerRandomShardPartyPicker,
                                                                          SchedulerPartyCache schedulerPartyCache,
                                                                          InitiateTransferCache initiateTransferContractIdCache) {
        if (adapterConfiguration.mode() == AdapterMode.SCHEDULER) {
            logger.info("created EnrichedPacs008SwiftToDamlTranslation under SCHEDULER mode");
            return new EnrichedPacs008SwiftToDamlTranslation(partyManager, assemblerRandomShardPartyPicker, schedulerPartyCache, initiateTransferContractIdCache);
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    public ApproveRejectProposalToDamlTranslation getApproveRejectToDamlTranslation(AdapterConfiguration adapterConfiguration,
                                                                                    PartyManager partyManager,
                                                                                    BankPartyCache bankPartyCache,
                                                                                    TransferProposalCache transferProposalCache) {
        if (adapterConfiguration.mode() == AdapterMode.BANK) {
            logger.info("created ApproveRejectProposalToDamlTranslation under BANK mode");
            return new ApproveRejectProposalToDamlTranslation(bankPartyCache, transferProposalCache);
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    public FinalizeRejectSettlementToDamlTranslation getFinalizeRejectSettlementToDamlTranslation(AdapterConfiguration adapterConfiguration,
                                                                            AssemblerPartyCache assemblerPartyCache,
                                                                            TransactionManifestCache transactionManifestCache) {
        if (adapterConfiguration.mode() == AdapterMode.ASSEMBLER) {
            logger.info("created FinalizeRejectSettlementToDamlTranslation under ASSEMBLER mode");
            return new FinalizeRejectSettlementToDamlTranslation(assemblerPartyCache, transactionManifestCache);
        }
        return null;
    }
}
