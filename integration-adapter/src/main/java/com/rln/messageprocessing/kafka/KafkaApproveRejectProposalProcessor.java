/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.kafka;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.conversion.kafka2daml.ApproveRejectProposalToDamlTranslation;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaApproveRejectProposalProcessor extends MessageProcessor<ApproveRejectProposal> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaApproveRejectProposalProcessor.class);
    private final RLNClient rlnClient;
    private final ApproveRejectProposalToDamlTranslation kafkaToDaml;
    private final PartyManager partyManager;

    public KafkaApproveRejectProposalProcessor(RLNClient rlnClient, ApproveRejectProposalToDamlTranslation kafkaToDaml, PartyManager partyManager) {
        logger.info("Created KafkaApproveRejectProposalProcessor with RLNClient {}, translation {}", rlnClient, kafkaToDaml);
        this.rlnClient = rlnClient;
        this.kafkaToDaml = kafkaToDaml;
        this.partyManager = partyManager;
    }

    @Override
    public void publish(ApproveRejectProposal messagePayload) {
        logger.info("Received ApproveRejectProposal with GroupId: {}", messagePayload.getGroupId());
        logger.debug("GroupId: {}, MessagePayload: {}", messagePayload.getGroupId(), messagePayload);
        String ownerBankBic = messagePayload.getBankBic();

        if (!partyManager.hasBic(ownerBankBic)) {
            logger.info("ApproveReject owned by ownerBankBic {} is only witnessed by current entity, ignored", ownerBankBic);
            return;
        }
        rlnClient.exerciseApproveRejectProposalChoice(messagePayload.getGroupId(), kafkaToDaml.apply(messagePayload));
    }
}
