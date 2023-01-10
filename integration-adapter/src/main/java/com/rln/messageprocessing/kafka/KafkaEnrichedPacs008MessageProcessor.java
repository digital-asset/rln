/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.kafka;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.kafkaClient.message.EnrichedPacs008;
import com.rln.conversion.kafka2daml.EnrichedPacs008SwiftToDamlTranslation;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaEnrichedPacs008MessageProcessor extends MessageProcessor<EnrichedPacs008> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaEnrichedPacs008MessageProcessor.class);
    private final RLNClient rlnClient;
    private final EnrichedPacs008SwiftToDamlTranslation translation;

    public KafkaEnrichedPacs008MessageProcessor(RLNClient rlnClient, EnrichedPacs008SwiftToDamlTranslation translation) {
        logger.info("Created KafkaEnrichedPacs008MessageProcessor with RLNClient {}, translation {}", rlnClient, translation);
        this.rlnClient = rlnClient;
        this.translation = translation;
    }

    @Override
    public void publish(EnrichedPacs008 enrichedPacs008) {
        logger.info("Received EnrichedPacs008 with GroupId: {}", enrichedPacs008.getGroupId());
        logger.debug("EnrichedPacs008 GroupId: {}, Message: {}", enrichedPacs008.getGroupId(), enrichedPacs008);
        var parameters = translation.apply(enrichedPacs008);
        rlnClient.exerciseCreateProposalsChoice(enrichedPacs008.getGroupId(), parameters);
    }
}
