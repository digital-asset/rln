/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing;

import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.PartyCache.AssemblerPartyCache;
import com.rln.cache.PartyCache.BankPartyCache;
import com.rln.cache.PartyCache.SchedulerPartyCache;
import com.rln.client.damlClient.RLNClient;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.*;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.common.IAConstants;
import com.rln.conversion.daml2kafka.*;
import com.rln.conversion.kafka2daml.ApproveRejectProposalToDamlTranslation;
import com.rln.conversion.kafka2daml.EnrichedPacs008SwiftToDamlTranslation;
import com.rln.conversion.kafka2daml.FinalizeRejectSettlementToDamlTranslation;
import com.rln.conversion.kafka2daml.InitiationPacs008SwiftToDamlTranslation;
import com.rln.messageprocessing.daml.*;
import com.rln.messageprocessing.kafka.KafkaApproveRejectProposalProcessor;
import com.rln.messageprocessing.kafka.KafkaEnrichedPacs008MessageProcessor;
import com.rln.messageprocessing.kafka.KafkaFinalizeRejectSettlementProcessor;
import com.rln.messageprocessing.kafka.KafkaInitiationMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class MessageProcessingProducer {

    Logger logger = LoggerFactory.getLogger(MessageProcessingProducer.class);

    @ApplicationScoped
    @Produces
    KafkaInitiationMessageProcessor getLedgerAdapterProcessor(RLNClient rlnClient,
                                                              InitiationPacs008SwiftToDamlTranslation translation) {
        logger.info("Created KafkaInitiationMessageProcessor with rlnClient {}, translation {}", rlnClient, translation);
        return new KafkaInitiationMessageProcessor(rlnClient, translation);
    }

    @ApplicationScoped
    @Produces
    DamlInitiationContractCreationProcessor getInitiationMessageProcessor(@Named(IAConstants.KAFKA_INITIATE_TRANSFER_SUBMITTER)
                                                                                  KafkaSubmitter<InitiateTransfer> kafkaSubmitter,
                                                                          InitiateTransferContractCreationToKafka conversion,
                                                                          InitiateTransferCache initiateTransferCache,
                                                                          SchedulerPartyCache schedulerPartyCache) {
        return new DamlInitiationContractCreationProcessor(kafkaSubmitter, conversion, initiateTransferCache, schedulerPartyCache);
    }

    @ApplicationScoped
    @Produces
    DamlFinalizeRejectSettlementChoiceExerciseProcessor getDamlFinalizeMessageProcessor(@Named(IAConstants.KAFKA_FINALIZE_REJECT_SETTLEMENT_SUBMITTER)
                                                                                                KafkaSubmitter<FinalizeRejectSettlement> kafkaSubmitter,
                                                                                        FinalizeRejectSettlementChoiceExerciseToKafka conversion) {
        return new DamlFinalizeRejectSettlementChoiceExerciseProcessor(kafkaSubmitter, conversion);
    }

    @ApplicationScoped
    @Produces
    DamlApproveRejectProposalChoiceExerciseProcessor getDamlApproveRejectMessageProcessor(@Named(IAConstants.KAFKA_APPROVE_REJECT_PROPOSAL_SUBMITTER)
                                                                                                  KafkaSubmitter<ApproveRejectProposal> kafkaSubmitter,
                                                                                          ApproveRejectProposalChoiceExerciseToKafka conversion) {
        return new DamlApproveRejectProposalChoiceExerciseProcessor(kafkaSubmitter, conversion);
    }

    @ApplicationScoped
    @Produces
    BankDamlTransferProposalContractCreationProcessor getDamlProposalMessageProcessor(@Named(IAConstants.KAFKA_TRANSFER_PROPOSAL_SUBMITTER)
                                                                                              KafkaSubmitter<TransferProposal> kafkaSubmitter,
                                                                                      TransferProposalContractCreationToKafka transferProposalContractCreationToKafka,
                                                                                      BankPartyCache bankPartyCache,
                                                                                      TransferProposalCache transferProposalCache,
                                                                                      PartyManager partyManager) {
        return new BankDamlTransferProposalContractCreationProcessor(kafkaSubmitter, transferProposalContractCreationToKafka, bankPartyCache, transferProposalCache, partyManager);
    }

    @ApplicationScoped
    @Produces
    AssemblerDamlTransferProposalContractCreationProcessor getDamlTransferProposalContractCreationCachingProcessor(TransferProposalCache transferProposalCache,
                                                                                                                   PartyManager partyManager) {
        return new AssemblerDamlTransferProposalContractCreationProcessor(transferProposalCache, partyManager);
    }

    @ApplicationScoped
    @Produces
    BankDamlTransactionManifestContractCreationProcessor getDamlTransactionManifestContractCreationCachingProcessor(TransactionManifestCache transactionManifestCache) {
        return new BankDamlTransactionManifestContractCreationProcessor(transactionManifestCache);
    }

    @ApplicationScoped
    @Produces
    AssemblerDamlTransactionManifestContractCreationProcessor getDamlTransactionManifestMessageProcessor(@Named(IAConstants.KAFKA_TRANSACTION_MANIFEST_SUBMITTER)
                                                                                                                 KafkaSubmitter<TransactionManifest> kafkaSubmitter,
                                                                                                         TransactionManifestContractCreationToKafka transactionManifestContractCreationToKafka,
                                                                                                         AssemblerPartyCache assemblerPartyCache,
                                                                                                         TransactionManifestCache transactionManifestCache) {
        return new AssemblerDamlTransactionManifestContractCreationProcessor(kafkaSubmitter, transactionManifestContractCreationToKafka, transactionManifestCache, assemblerPartyCache);
    }

    @ApplicationScoped
    @Produces
    KafkaFinalizeRejectSettlementProcessor getKafkaFinalizeRejectSettlementProcessor(RLNClient rlnClient, FinalizeRejectSettlementToDamlTranslation kafkaToDaml) {
        return new KafkaFinalizeRejectSettlementProcessor(rlnClient, kafkaToDaml);
    }

    @ApplicationScoped
    @Produces
    KafkaApproveRejectProposalProcessor getKafkaApproveRejectProcessor(RLNClient rlnClient, ApproveRejectProposalToDamlTranslation kafkaToDaml,
                                                                       PartyManager partyManager) {
        return new KafkaApproveRejectProposalProcessor(rlnClient, kafkaToDaml, partyManager);
    }

    @ApplicationScoped
    @Produces
    KafkaEnrichedPacs008MessageProcessor getKafkaEnrichedPacs008Processor(RLNClient rlnClient, EnrichedPacs008SwiftToDamlTranslation enrichedPacs008SwiftToDamlTranslation) {
        return new KafkaEnrichedPacs008MessageProcessor(rlnClient, enrichedPacs008SwiftToDamlTranslation);
    }
}
