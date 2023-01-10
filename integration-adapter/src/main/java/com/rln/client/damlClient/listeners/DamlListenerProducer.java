/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners;

import com.rln.client.damlClient.listeners.creation.InitiationDamlListener;
import com.rln.client.damlClient.listeners.creation.ProposalDamlListener;
import com.rln.client.damlClient.listeners.creation.TransactionManifestDamlListener;
import com.rln.client.damlClient.listeners.exercise.ApproveRejectProposalDamlListener;
import com.rln.client.damlClient.listeners.exercise.FinalizeRejectSettlementDamlListener;
import com.rln.client.damlClient.partyManagement.ShardPartyReader;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.common.IAConstants;
import com.rln.configuration.AdapterConfiguration;
import com.rln.configuration.AdapterMode;
import com.rln.messageprocessing.daml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

public class DamlListenerProducer {

    Logger logger = LoggerFactory.getLogger(DamlListenerProducer.class);

    @Singleton
    @Produces
    public InitiationDamlListener getInitiationDamlListener(RLNLedgerSubscriber subscriber,
                                                            DamlInitiationContractCreationProcessor messageProcessor,
                                                            AdapterConfiguration adapterConfiguration,
                                                            @Named(IAConstants.SCHEDULER_SHARD_PARTY_READER)
                                                                    ShardPartyReader schedulerShardPartyReader) {
        if (adapterConfiguration.mode().equals(AdapterMode.SCHEDULER)) {
            logger.info("Created InitiationDamlListener under SCHEDULER mode");
            InitiationDamlListener initiationDamlListener = new InitiationDamlListener(
                    schedulerShardPartyReader.getShardParties(),
                    subscriber,
                    messageProcessor);
            initiationDamlListener.subscribe();
            return initiationDamlListener;
        }
        return null;
    }

    @Singleton
    @Produces
    public FinalizeRejectSettlementDamlListener getFinalizeDamlListener(RLNLedgerSubscriber subscriber,
                                                                        DamlFinalizeRejectSettlementChoiceExerciseProcessor messageProcessor,
                                                                        AdapterConfiguration adapterConfiguration,
                                                                        @Named(IAConstants.BANK_SHARD_PARTY_READER)
                                                                ShardPartyReader bankShardPartyReader) {
        if (adapterConfiguration.mode().equals(AdapterMode.BANK)) {
            logger.info("Created FinalizeDamlListener under BANK mode");
            FinalizeRejectSettlementDamlListener finalizeRejectSettlementDamlListener = new FinalizeRejectSettlementDamlListener(
                    bankShardPartyReader.getShardParties(),
                    subscriber,
                    messageProcessor);
            finalizeRejectSettlementDamlListener.subscribe();
            return finalizeRejectSettlementDamlListener;
        }
        return null;
    }

    @Singleton
    @Produces
    public ProposalDamlListener getProposalDamlListener(RLNLedgerSubscriber subscriber,
                                                        BankDamlTransferProposalContractCreationProcessor bankMessageProcessor,
                                                        AssemblerDamlTransferProposalContractCreationProcessor assemblerMessageProcessor,
                                                        AdapterConfiguration adapterConfiguration,
                                                        @Named(IAConstants.BANK_SHARD_PARTY_READER)
                                                                ShardPartyReader bankShardPartyReader,
                                                        @Named(IAConstants.ASSEMBLER_SHARD_PARTY_READER)
                                                                ShardPartyReader assemblerShardPartyReader) {
        ProposalDamlListener proposalDamlListener = null;
        if (adapterConfiguration.mode().equals(AdapterMode.BANK)) {
            logger.info("Created ProposalDamlListener under BANK mode");
            proposalDamlListener = new ProposalDamlListener(
                    bankShardPartyReader.getShardParties(),
                    subscriber,
                    bankMessageProcessor);
            proposalDamlListener.subscribe();
        } else if (adapterConfiguration.mode().equals(AdapterMode.ASSEMBLER)) {
            logger.info("Created ProposalDamlListener under ASSEMBLER mode");
            proposalDamlListener = new ProposalDamlListener(
                    assemblerShardPartyReader.getShardParties(),
                    subscriber,
                    assemblerMessageProcessor);
            proposalDamlListener.subscribe();
        }
        return proposalDamlListener;
    }

    @Singleton
    @Produces
    public TransactionManifestDamlListener getTransactionManifestDamlListener(RLNLedgerSubscriber subscriber,
                                                                              AssemblerDamlTransactionManifestContractCreationProcessor assemblerMessageProcessor,
                                                                              BankDamlTransactionManifestContractCreationProcessor bankMessageProcessor,
                                                                              AdapterConfiguration adapterConfiguration,
                                                                              @Named(IAConstants.ASSEMBLER_SHARD_PARTY_READER)
                                                                                      ShardPartyReader assemblerShardPartyReader,
                                                                              @Named(IAConstants.BANK_SHARD_PARTY_READER)
                                                                                      ShardPartyReader bankShardPartyReader) {
        TransactionManifestDamlListener transactionManifestDamlListener = null;
        if (adapterConfiguration.mode().equals(AdapterMode.ASSEMBLER)) {
            transactionManifestDamlListener = new TransactionManifestDamlListener(
                    assemblerShardPartyReader.getShardParties(),
                    subscriber,
                    assemblerMessageProcessor);
            transactionManifestDamlListener.subscribe();
            return transactionManifestDamlListener;
        } else if (adapterConfiguration.mode().equals(AdapterMode.BANK)) {
            transactionManifestDamlListener = new TransactionManifestDamlListener(
                    bankShardPartyReader.getShardParties(),
                    subscriber,
                    bankMessageProcessor);
            transactionManifestDamlListener.subscribe();
        }
        return transactionManifestDamlListener;
    }

    @Singleton
    @Produces
    public ApproveRejectProposalDamlListener getAcceptRejectDamlListener(RLNLedgerSubscriber subscriber,
                                                                         DamlApproveRejectProposalChoiceExerciseProcessor messageProcessor,
                                                                         AdapterConfiguration adapterConfiguration,
                                                                         @Named(IAConstants.ASSEMBLER_SHARD_PARTY_READER)
                                                                        ShardPartyReader assemblerShardPartyReader) {
        if (adapterConfiguration.mode().equals(AdapterMode.ASSEMBLER)) {
            ApproveRejectProposalDamlListener approveRejectProposalDamlListener = new ApproveRejectProposalDamlListener(
                    assemblerShardPartyReader.getShardParties(),
                    subscriber,
                    messageProcessor);
            approveRejectProposalDamlListener.subscribe();
            return approveRejectProposalDamlListener;
        }
        return null;
    }
}
