/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient;

import com.rln.client.kafkaClient.message.*;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.common.IAConstants;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

public class KafkaProducer {

    @ApplicationScoped
    @Named(IAConstants.KAFKA_INITIATE_TRANSFER_SUBMITTER)
    KafkaSubmitter<InitiateTransfer> getKafkaInitiateTransferSubmitter(
            @Channel("initiation-message-out") Emitter<InitiateTransfer> initiateTransferEmitter) {
        return new KafkaSubmitter<>(initiateTransferEmitter);
    }

    @ApplicationScoped
    @Named(IAConstants.KAFKA_FINALIZE_REJECT_SETTLEMENT_SUBMITTER)
    KafkaSubmitter<FinalizeRejectSettlement> getKafkaFinalizeRejectSettlementSubmitter(
            @Channel("finalize-reject-settlement-message-out") Emitter<FinalizeRejectSettlement> finalizeRejectSettlementEmitter) {
        return new KafkaSubmitter<>(finalizeRejectSettlementEmitter);
    }

    @ApplicationScoped
    @Named(IAConstants.KAFKA_APPROVE_REJECT_PROPOSAL_SUBMITTER)
    KafkaSubmitter<ApproveRejectProposal> getKafkaApproveRejectProposalSubmitter(
            @Channel("approve-reject-proposal-message-out") Emitter<ApproveRejectProposal> approveRejectProposalSubmitter) {
        return new KafkaSubmitter<>(approveRejectProposalSubmitter);
    }

    @ApplicationScoped
    @Named(IAConstants.KAFKA_TRANSFER_PROPOSAL_SUBMITTER)
    KafkaSubmitter<TransferProposal> getKafkaTransferProposalSubmitter(
        @Channel("transfer-proposal-message-out") Emitter<TransferProposal> transferProposalEmitter) {
        return new KafkaSubmitter<>(transferProposalEmitter);
    }

    @ApplicationScoped
    @Named(IAConstants.KAFKA_TRANSACTION_MANIFEST_SUBMITTER)
    KafkaSubmitter<TransactionManifest> getKafkaTransactionManifestSubmitter(
            @Channel("transaction-manifest-message-out") Emitter<TransactionManifest> transactionManifestEmitter) {
        return new KafkaSubmitter<>(transactionManifestEmitter);
    }
}
