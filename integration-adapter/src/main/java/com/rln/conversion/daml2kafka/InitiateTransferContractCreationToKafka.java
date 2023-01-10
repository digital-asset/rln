/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.InitiateTransfer;

public class InitiateTransferContractCreationToKafka implements CreatedEventConverter<InitiateTransfer> {
    private final PartyManager partyManager;

    public InitiateTransferContractCreationToKafka(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public InitiateTransfer createdEventToKafka(CreatedEvent event) {
        com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer damlInitiateTransfer = com.rln.damlCodegen.workflow.initiatetransfer
                .InitiateTransfer.fromValue(event.getArguments());
        String initiatorBic = partyManager.getBic(damlInitiateTransfer.initiator);
        return new InitiateTransfer(damlInitiateTransfer.groupId, initiatorBic, damlInitiateTransfer.payload);
    }
}
