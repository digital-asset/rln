/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.daml;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.conversion.daml2kafka.FinalizeRejectSettlementChoiceExerciseToKafka;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DamlFinalizeRejectSettlementChoiceExerciseProcessor extends MessageProcessor<ExercisedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DamlFinalizeRejectSettlementChoiceExerciseProcessor.class);
    private final KafkaSubmitter<FinalizeRejectSettlement> kafkaSubmitter;
    private final FinalizeRejectSettlementChoiceExerciseToKafka conversion;


    public DamlFinalizeRejectSettlementChoiceExerciseProcessor(KafkaSubmitter<FinalizeRejectSettlement> kafkaSubmitter,
                                                               FinalizeRejectSettlementChoiceExerciseToKafka conversion) {
        logger.info("Created DamlFinalizeRejectSettlementChoiceExerciseProcessor with translation {}", conversion);
        this.kafkaSubmitter = kafkaSubmitter;
        this.conversion = conversion;
    }

    @Override
    public void publish(ExercisedEvent msg) {
        logger.info("DamlFinalizeRejectSettlementChoiceExerciseProcessor about to publish a message with EventId: {}", msg.getEventId());
        logger.debug("EventId: {} Message: {}", msg.getEventId(), msg);
        var converted = conversion.exercisedEventToKafka(msg);
        logger.info("Publishing to Kafka, GroupId: {}", converted.getGroupId());
        kafkaSubmitter.submit(converted);
        logger.debug("Published to Kafka, GroupId: {}, Message: {}", converted.getGroupId(), converted);
    }
}
