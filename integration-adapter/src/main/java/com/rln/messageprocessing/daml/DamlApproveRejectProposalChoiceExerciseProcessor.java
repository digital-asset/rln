/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.daml;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.conversion.daml2kafka.ApproveRejectProposalChoiceExerciseToKafka;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DamlApproveRejectProposalChoiceExerciseProcessor extends MessageProcessor<ExercisedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DamlApproveRejectProposalChoiceExerciseProcessor.class);
    private final KafkaSubmitter<ApproveRejectProposal> kafkaSubmitter;
    private final ApproveRejectProposalChoiceExerciseToKafka conversion;


    public DamlApproveRejectProposalChoiceExerciseProcessor(KafkaSubmitter<ApproveRejectProposal> kafkaSubmitter,
                                                            ApproveRejectProposalChoiceExerciseToKafka conversion) {
        logger.info("Created DamlApproveRejectProposalChoiceExerciseProcessor with translation {}", conversion);
        this.kafkaSubmitter = kafkaSubmitter;
        this.conversion = conversion;
    }

    @Override
    public void accept(ExercisedEvent msg) {
        logger.info("DamlApproveRejectProposalChoiceExerciseProcessor about to publish a message with EventId: {}", msg.getEventId());
        logger.debug("EventId: {} Message: {}", msg.getEventId(), msg);
        var converted = conversion.exercisedEventToKafka(msg);
        logger.info("Publishing to Kafka, GroupId: {}", converted.getGroupId());
        kafkaSubmitter.submit(converted);
        logger.debug("Published to Kafka, GroupId: {}, Message: {}", converted.getGroupId(), converted);
    }
}
