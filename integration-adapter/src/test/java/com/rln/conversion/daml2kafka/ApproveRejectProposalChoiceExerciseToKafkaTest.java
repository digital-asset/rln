/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.rln.CommonBaseTest;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.exception.ValueNotFoundException;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.common.Constants;
import com.rln.damlCodegen.workflow.transferproposal.ApproveProposal;
import com.rln.damlCodegen.workflow.transferproposal.RejectProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Optional;

class ApproveRejectProposalChoiceExerciseToKafkaTest extends CommonBaseTest {

    private static final TransferProposalCache cache = new TransferProposalCache();
    private static final ApproveRejectProposalChoiceExerciseToKafka conversion =
            new ApproveRejectProposalChoiceExerciseToKafka(cache);
    private static final TransferProposalKey key = new TransferProposalKey(GROUP_ID, MESSAGE_ID, BANK11_BIC);
    private static final TransferProposal.ContractId cid = new TransferProposal.ContractId(CONTRACT_ID);
    private static final ExercisedEvent exercisedEvent = getExercisedEvent(TransferProposal.TEMPLATE_ID);

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {REASON})
    void GIVEN_contractId_to_proposal_key_mapping_exist_and_daml_approve_proposal_WHEN_convert_THEN_convert_correctly(String reason) {
        // GIVEN
        cache.write(key, cid);

        ApproveProposal approve = new ApproveProposal(Optional.ofNullable(reason), false);
        Mockito.when(exercisedEvent.getContractId()).thenReturn(CONTRACT_ID);
        Mockito.when(exercisedEvent.getChoiceArgument()).thenReturn(approve.toValue());
        Mockito.when(exercisedEvent.getChoice()).thenReturn(Constants.APPROVE_PROPOSAL_CHOICE);

        // WHEN
        ApproveRejectProposal result = conversion.exercisedEventToKafka(exercisedEvent);

        // THEN
        MatcherAssert.assertThat(result.getGroupId(), Matchers.is(GROUP_ID));
        MatcherAssert.assertThat(result.getMessageId(), Matchers.is(MESSAGE_ID));
        MatcherAssert.assertThat(result.getReason(), Matchers.is(reason));
        MatcherAssert.assertThat(result.getStatus(), Matchers.is(Status.APPROVE));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {REASON})
    void GIVEN_contractId_to_proposal_key_mapping_exist_and_daml_reject_proposal_with_reason_WHEN_convert_THEN_convert_correctly(String reason) {
        // GIVEN
        cache.write(key, cid);
        RejectProposal reject = new RejectProposal(Optional.ofNullable(reason));

        Mockito.when(exercisedEvent.getContractId()).thenReturn(CONTRACT_ID);
        Mockito.when(exercisedEvent.getChoiceArgument()).thenReturn(reject.toValue());
        Mockito.when(exercisedEvent.getChoice()).thenReturn(Constants.REJECT_PROPOSAL_CHOICE);

        // WHEN
        ApproveRejectProposal result = conversion.exercisedEventToKafka(exercisedEvent);

        // THEN
        MatcherAssert.assertThat(result.getGroupId(), Matchers.is(GROUP_ID));
        MatcherAssert.assertThat(result.getMessageId(), Matchers.is(MESSAGE_ID));
        MatcherAssert.assertThat(result.getReason(), Matchers.is(reason));
        MatcherAssert.assertThat(result.getStatus(), Matchers.is(Status.REJECT));
    }

    @Test
    void GIVEN_non_existing_contractId_and_daml_approve_proposal_WHEN_convert_THEN_throw_UncachedContractIdException_exception() {
        // GIVEN
        ApproveProposal approve = new ApproveProposal(Optional.of(REASON), false);
        Mockito.when(exercisedEvent.getContractId()).thenReturn("Non exising contractId");
        Mockito.when(exercisedEvent.getChoiceArgument()).thenReturn(approve.toValue());
        Mockito.when(exercisedEvent.getChoice()).thenReturn(Constants.APPROVE_PROPOSAL_CHOICE);

        // WHEN + THEN
        Assertions.assertThrows(ValueNotFoundException.class, () -> conversion.exercisedEventToKafka(exercisedEvent));
    }

    @Test
    void GIVEN_non_existing_contractId_and_daml_reject_proposal_WHEN_convert_THEN_throw_UncachedContractIdException_exception() {
        // GIVEN
        RejectProposal reject = new RejectProposal(Optional.of(REASON));
        Mockito.when(exercisedEvent.getContractId()).thenReturn("Non exising contractId");
        Mockito.when(exercisedEvent.getChoiceArgument()).thenReturn(reject.toValue());
        Mockito.when(exercisedEvent.getChoice()).thenReturn(Constants.REJECT_PROPOSAL_CHOICE);

        // WHEN + THEN
        Assertions.assertThrows(ValueNotFoundException.class, () -> conversion.exercisedEventToKafka(exercisedEvent));
    }
}
