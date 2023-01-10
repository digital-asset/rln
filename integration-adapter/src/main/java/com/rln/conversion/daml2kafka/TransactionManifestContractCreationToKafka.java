/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.TransactionManifest;
import com.rln.client.kafkaClient.message.fields.MessageWithBics;
import com.rln.damlCodegen.da.types.Tuple2;

import java.util.List;

public class TransactionManifestContractCreationToKafka implements CreatedEventConverter<TransactionManifest> {
    private final PartyManager partyManager;

    public TransactionManifestContractCreationToKafka(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public TransactionManifest createdEventToKafka(CreatedEvent event) {
        com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest damlTransactionManifest = com.rln.damlCodegen.workflow.transactionmanifest
                .TransactionManifest.fromValue(event.getArguments());

        MessageWithBics[] messageWithBic = damlTransactionManifest.messageIdToApprovers.stream()
                .map(this::toMessageWithBic).toArray(MessageWithBics[]::new);

        return new TransactionManifest(damlTransactionManifest.groupId, messageWithBic);
    }

    private MessageWithBics toMessageWithBic(Tuple2<String, List<String>> messageIdToApprovers) {
        String messageId = messageIdToApprovers._1;
        if (messageId == null) {
            throw new IllegalArgumentException("messageId should not be null in messageIdToApprovers map");
        }
        String[] approverBics = messageIdToApprovers._2.stream()
                .map(partyManager::getBic)
                .toArray(String[]::new);
        return new MessageWithBics(messageId, approverBics);
    }
}
