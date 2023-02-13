/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.client.damlClient.partyManagement.PartyManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class Daml2KafkaProducer {

    @ApplicationScoped
    @Produces
    public InitiateTransferContractCreationToKafka getInitiationDamlMessageToKafka(PartyManager partyManager) {
        return new InitiateTransferContractCreationToKafka(partyManager);
    }

    @ApplicationScoped
    @Produces
    public FinalizeRejectSettlementChoiceExerciseToKafka getFinalizeDamlMessageToKafka(TransactionManifestCache transactionManifestCache) {
        return new FinalizeRejectSettlementChoiceExerciseToKafka(transactionManifestCache);
    }

    @ApplicationScoped
    @Produces
    public ApproveRejectProposalChoiceExerciseToKafka getApproveRejectMessageToKafka(PartyManager partyManager) {
        return new ApproveRejectProposalChoiceExerciseToKafka(partyManager);
    }

    @ApplicationScoped
    @Produces
    public TransferProposalContractCreationToKafka getTransferProposalToKafka() {
        return new TransferProposalContractCreationToKafka();
    }

    @ApplicationScoped
    @Produces
    public TransactionManifestContractCreationToKafka getTransactionManifestToKafka(PartyManager partyManager) {
        return new TransactionManifestContractCreationToKafka(partyManager);
    }
}
