/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.kafka;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.kafkaClient.message.InitiateTransfer;
import com.rln.conversion.kafka2daml.InitiationPacs008SwiftToDamlTranslation;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaInitiationMessageProcessor extends MessageProcessor<InitiateTransfer> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaInitiationMessageProcessor.class);
    private final RLNClient rlnClient;
    private final InitiationPacs008SwiftToDamlTranslation swiftToDaml;

    public KafkaInitiationMessageProcessor(RLNClient rlnClient, InitiationPacs008SwiftToDamlTranslation swiftToDaml) {
        logger.info("Created KafkaInitiationMessageProcessor with RLNClient {}, translation {}", rlnClient, swiftToDaml);
        this.rlnClient = rlnClient;
        this.swiftToDaml = swiftToDaml;
    }

    @Override
    public void publish(InitiateTransfer messagePayload) {
        logger.info("Received InitiateTransfer with GroupId: {}", messagePayload.getGroupId());
        logger.debug("InitiateTransfer GroupId: {}, Message: {}", messagePayload.getGroupId(), messagePayload);
        rlnClient.createInitiateTransferContract(swiftToDaml.apply(messagePayload));
    }
}
