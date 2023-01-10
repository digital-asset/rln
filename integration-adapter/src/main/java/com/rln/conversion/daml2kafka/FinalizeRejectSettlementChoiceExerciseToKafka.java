/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.daml.ledger.javaapi.data.Value;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.common.Constants;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.workflow.transactionmanifest.FinalizeSettlement;
import com.rln.damlCodegen.workflow.transactionmanifest.RejectSettlement;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;

public class FinalizeRejectSettlementChoiceExerciseToKafka implements ExercisedEventConverter<FinalizeRejectSettlement> {


    private final TransactionManifestCache transactionManifestCache;

    public FinalizeRejectSettlementChoiceExerciseToKafka(TransactionManifestCache transactionManifestCache) {
        this.transactionManifestCache = transactionManifestCache;
    }

    @Override
    public FinalizeRejectSettlement exercisedEventToKafka(ExercisedEvent event) {
        var contractId = new TransactionManifest.ContractId(event.getContractId());

        String groupId = transactionManifestCache.readFromValueToKey(contractId);
        Status status = getStatusFromChoice(event.getChoice());
        String reason = getReason(status, event.getChoiceArgument());

        return new FinalizeRejectSettlement(groupId, status, reason);
    }

    private String getReason(Status status, Value choiceArgs) {
        switch (status) {
            case APPROVE:
                return FinalizeSettlement.fromValue(choiceArgs).reason.orElse(IAConstants.JSON_NULL_STRING);
            case REJECT:
                return RejectSettlement.fromValue(choiceArgs).reason.orElse(IAConstants.JSON_NULL_STRING);
            default:
                throw new IllegalArgumentException("FinalizeChoiceExerciseToKafka: Unexpected status '" + status + "'");
        }
    }

    private Status getStatusFromChoice(String choice) {
        switch (choice) {
            case Constants.FINALIZE_SETTLEMENT_CHOICE:
                return Status.APPROVE;
            case Constants.REJECT_SETTLEMENT_CHOICE:
                return Status.REJECT;
            default:
                throw new IllegalArgumentException("FinalizeChoiceExerciseToKafka: Unexpected choice '" + choice + "'");
        }
    }
}
