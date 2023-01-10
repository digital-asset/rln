/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.kafka;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.conversion.kafka2daml.FinalizeRejectSettlementToDamlTranslation;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaFinalizeRejectSettlementProcessor extends MessageProcessor<FinalizeRejectSettlement> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaFinalizeRejectSettlementProcessor.class);
    private final RLNClient rlnClient;
    private final FinalizeRejectSettlementToDamlTranslation translation;

    public KafkaFinalizeRejectSettlementProcessor(RLNClient rlnClient, FinalizeRejectSettlementToDamlTranslation translation) {
        logger.info("Created KafkaFinalizeRejectSettlementProcessor with RLNClient {}, translation {}", rlnClient, translation);
        this.rlnClient = rlnClient;
        this.translation = translation;
    }

    @Override
    protected void publish(FinalizeRejectSettlement input) {
        logger.info("Received FinalizeRejectSettlement with GroupId: {}", input.getGroupId());
        logger.debug("FinalizeRejectSettlement GroupId: {}, Message: {}", input.getGroupId(), input);
        this.rlnClient.exerciseFinalizeRejectSettlement(input.getGroupId(), translation.apply(input));
    }
}
