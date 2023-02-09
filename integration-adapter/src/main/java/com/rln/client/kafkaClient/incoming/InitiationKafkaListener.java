/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.incoming;

import com.rln.client.kafkaClient.message.InitiateTransfer;
import com.rln.messageprocessing.MessageProcessor;
import com.rln.messageprocessing.kafka.KafkaInitiationMessageProcessor;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class InitiationKafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(InitiationKafkaListener.class);
    MessageProcessor<InitiateTransfer> processor;

    public InitiationKafkaListener(KafkaInitiationMessageProcessor processor) {
      logger.info("Created InitiationKafkaListener with processor {}", processor);
        this.processor = processor;
    }

    @Incoming("initiation-message-in")
    public void acceptInitiationMessage(String message) {
        // To disable this, see:
        // https://github.com/quarkusio/quarkus/issues/19318
        // https://quarkus.io/guides/kafka#kafka-configuration
        // https://github.com/quarkusio/quarkus/issues/4114#issuecomment-533465076

        // For transactionality:
        // https://smallrye.io/smallrye-reactive-messaging/3.16.0/kafka/transactions/#exactly-once-processing
        // https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.1/kafka/kafka.html#_receiving_kafka_records_in_batches
        // https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.3/acknowledgement/acknowledgement.html#_acknowledgment_when_using_messages
      var payload = MessageExtractor.extractAs(message, InitiateTransfer.class);
      payload.ifPresent(processor);
    }
}
