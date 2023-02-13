/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.workflow.transferproposal.*;

public class ApproveRejectProposalChoiceExerciseToKafka implements CreatedEventConverter<ApproveRejectProposal> {

    private final PartyManager partyManager;

    public ApproveRejectProposalChoiceExerciseToKafka(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public ApproveRejectProposal createdEventToKafka(CreatedEvent createdEvent) {
        if (createdEvent.getTemplateId().equals(ApprovedTransferProposal.TEMPLATE_ID)) {
            var contract = ApprovedTransferProposal.Contract.fromCreatedEvent(createdEvent);
            return new ApproveRejectProposal(
                    contract.data.groupId,
                    contract.data.messageId,
                    Status.APPROVE,
                    contract.data.reason.orElse(IAConstants.JSON_NULL_STRING),
                    partyManager.getBic(contract.data.owner)
            );
        } else if (createdEvent.getTemplateId().equals(RejectedTransferProposal.TEMPLATE_ID)) {
            var contract = RejectedTransferProposal.Contract.fromCreatedEvent(createdEvent);
            return new ApproveRejectProposal(
                    contract.data.groupId,
                    contract.data.messageId,
                    Status.REJECT,
                    contract.data.reason.orElse(IAConstants.JSON_NULL_STRING),
                    partyManager.getBic(contract.data.owner)
            );
        } else {
            throw new IllegalArgumentException("AcceptRejectProposalChoiceExerciseToKafka: Unexpected template '" + createdEvent.getTemplateId() + "'");
        }
    }
}
