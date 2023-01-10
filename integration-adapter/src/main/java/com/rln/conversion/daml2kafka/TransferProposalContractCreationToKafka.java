/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.client.kafkaClient.message.TransferProposal;

public class TransferProposalContractCreationToKafka implements CreatedEventConverter<TransferProposal> {
    @Override
    public TransferProposal createdEventToKafka(CreatedEvent event) {
        com.rln.damlCodegen.workflow.transferproposal.TransferProposal transferProposal = com.rln.damlCodegen.workflow.transferproposal
                .TransferProposal.fromValue(event.getArguments());
        return new TransferProposal(transferProposal.groupId, transferProposal.messageId, transferProposal.legPayload);
    }
}
