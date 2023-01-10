/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.daml.ledger.javaapi.data.Value;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.common.Constants;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.workflow.transferproposal.ApproveProposal;
import com.rln.damlCodegen.workflow.transferproposal.RejectProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;

public class ApproveRejectProposalChoiceExerciseToKafka implements ExercisedEventConverter<ApproveRejectProposal> {

    private final TransferProposalCache transferProposalCache;

    public ApproveRejectProposalChoiceExerciseToKafka(TransferProposalCache transferProposalCache) {
        this.transferProposalCache = transferProposalCache;
    }

    @Override
    public ApproveRejectProposal exercisedEventToKafka(ExercisedEvent exercisedEvent) {
        var contractId = new TransferProposal.ContractId(exercisedEvent.getContractId());
        TransferProposalKey transferProposalKey = transferProposalCache.readFromValueToKey(contractId);
        Status status = getStatusFromChoice(exercisedEvent.getChoice());
        String reason = getReason(status, exercisedEvent.getChoiceArgument());

        return new ApproveRejectProposal(transferProposalKey.getGroupId(),
                transferProposalKey.getMessageId(), status, reason, transferProposalKey.getBankBic());
    }

    private String getReason(Status status, Value choiceArgs) {
        switch (status) {
            case APPROVE:
                return ApproveProposal.fromValue(choiceArgs).reason.orElse(IAConstants.JSON_NULL_STRING);
            case REJECT:
                return RejectProposal.fromValue(choiceArgs).reason.orElse(IAConstants.JSON_NULL_STRING);
            default:
                throw new IllegalArgumentException("AcceptRejectProposalChoiceExerciseToKafka: Unexpected status '" + status + "'");
        }
    }

    private Status getStatusFromChoice(String choice) {
        switch (choice) {
            case Constants.APPROVE_PROPOSAL_CHOICE:
                return Status.APPROVE;
            case Constants.REJECT_PROPOSAL_CHOICE:
                return Status.REJECT;
            default:
                throw new IllegalArgumentException("AcceptRejectProposalChoiceExerciseToKafka: Unexpected choice '" + choice + "'");
        }
    }
}
