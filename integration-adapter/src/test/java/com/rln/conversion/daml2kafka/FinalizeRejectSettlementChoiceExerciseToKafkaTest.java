/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.daml.ledger.javaapi.data.Value;
import com.rln.CommonBaseTest;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.cache.exception.ValueNotFoundException;
import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.common.Constants;
import com.rln.damlCodegen.workflow.transactionmanifest.FinalizeSettlement;
import com.rln.damlCodegen.workflow.transactionmanifest.RejectSettlement;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.ContractId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class FinalizeRejectSettlementChoiceExerciseToKafkaTest extends CommonBaseTest {

    TransactionManifestCache cache = new TransactionManifestCache();
    FinalizeRejectSettlementChoiceExerciseToKafka converter = new FinalizeRejectSettlementChoiceExerciseToKafka(cache);

    {
        cache.write(GROUP_ID, new ContractId(CONTRACT_ID));
    }

    @Test
    void wrongChoiceNameThrows() {
        ExercisedEvent event = getEvent(CONTRACT_ID, "SomeChoice", getFinalizeChoiceArgument(Optional.of(REASON)));

        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.exercisedEventToKafka(event));
    }

    @Test
    void wrongChoiceArgumentThrows() {
        ExercisedEvent event = getEvent(CONTRACT_ID, Constants.FINALIZE_SETTLEMENT_CHOICE, new DamlRecord());

        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.exercisedEventToKafka(event));
    }

    @Test
    void noneAsReasonWorks() {
        var argument = getFinalizeChoiceArgument(Optional.empty());
        ExercisedEvent event = getEvent(CONTRACT_ID, Constants.FINALIZE_SETTLEMENT_CHOICE, argument);

        var result = converter.exercisedEventToKafka(event);
        Assertions.assertEquals(new FinalizeRejectSettlement(GROUP_ID, Status.APPROVE, null), result);
    }

    @Test
    void someTextAsReasonWithFinalizeChoiceWorks() {
        var argument = getFinalizeChoiceArgument(Optional.of(REASON));
        ExercisedEvent event = getEvent(CONTRACT_ID, Constants.FINALIZE_SETTLEMENT_CHOICE, argument);

        var result = converter.exercisedEventToKafka(event);
        Assertions.assertEquals(new FinalizeRejectSettlement(GROUP_ID, Status.APPROVE, REASON), result);
    }

    @Test
    void someTextAsReasonWithRejectChoiceIsHandled() {
        var argument = getRejectChoiceArgument(Optional.of(REASON));
        ExercisedEvent event = getEvent(CONTRACT_ID, Constants.REJECT_SETTLEMENT_CHOICE, argument);

        var result = converter.exercisedEventToKafka(event);
        Assertions.assertEquals(new FinalizeRejectSettlement(GROUP_ID, Status.REJECT, REASON), result);
    }

    @Test
    void uncachedContractIdThrows() {
        var argument = getRejectChoiceArgument(Optional.of(REASON));
        ExercisedEvent event = getEvent(CONTRACT_ID_2, Constants.REJECT_SETTLEMENT_CHOICE, argument);

        Assertions.assertThrows(ValueNotFoundException.class, () -> converter.exercisedEventToKafka(event));
    }

    private ExercisedEvent getEvent(String contractId, String choiceName, Value choiceArgument) {
        var result = Mockito.mock(ExercisedEvent.class);
        Mockito.when(result.getContractId()).thenReturn(contractId);
        Mockito.when(result.getChoice()).thenReturn(choiceName);
        Mockito.when(result.getChoiceArgument()).thenReturn(choiceArgument);
        return result;
    }

    private Value getFinalizeChoiceArgument(Optional<String> reason) {
        return new FinalizeSettlement(reason).toValue();
    }

    private Value getRejectChoiceArgument(Optional<String> reason) {
        return new RejectSettlement(reason).toValue();
    }
}
