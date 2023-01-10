/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.kafka2daml;

import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class InitiationPacs008SwiftToDamlTranslation implements Function<com.rln.client.kafkaClient.message.InitiateTransfer, InitiateTransfer> {

    private static final Logger logger = LoggerFactory.getLogger(InitiationPacs008SwiftToDamlTranslation.class);

    private final PartyManager partyManager;
    private final RandomShardPartyPicker randomShardPartyPicker;

    public InitiationPacs008SwiftToDamlTranslation(PartyManager partyManager, RandomShardPartyPicker randomShardPartyPicker) {
        logger.info("Created InitiationPacs008SwiftToDamlTranslation with PartyManager {} and RandomShardPartyPicker {}", partyManager, randomShardPartyPicker);
        this.partyManager = partyManager;
        this.randomShardPartyPicker = randomShardPartyPicker;
    }

    @Override
    public InitiateTransfer apply(com.rln.client.kafkaClient.message.InitiateTransfer initiateTransfer) {
        var initiatorPartyId = partyManager.getParty(initiateTransfer.getInitiator());
        var schedulerPartyId = randomShardPartyPicker.pickRandomShardParty();
        return new InitiateTransfer(initiateTransfer.getGroupId(), initiatorPartyId, schedulerPartyId, initiateTransfer.getPayload());
    }
}
