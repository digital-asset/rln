/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Template;
import com.rln.CommonBaseTest;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.damlCodegen.workflow.data.Instrument;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import com.rln.damlCodegen.workflow.data.ibans.SenderAndReceiver;
import com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.RejectedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ApproveRejectProposalChoiceExerciseToKafkaTest extends CommonBaseTest {

    private static final TransferProposalCache cache = new TransferProposalCache();
    private static final ApproveRejectProposalChoiceExerciseToKafka conversion =
            new ApproveRejectProposalChoiceExerciseToKafka(partyManager);
    private static final TransferProposalKey key = new TransferProposalKey(GROUP_ID, MESSAGE_ID, BANK11_BIC);
    private static final TransferProposal.ContractId cid = new TransferProposal.ContractId(CONTRACT_ID);

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {REASON})
    void GIVEN_contractId_to_proposal_key_mapping_exist_and_daml_approve_proposal_WHEN_convert_THEN_convert_correctly(String reason) {
        // GIVEN
        Mockito.when(partyManager.getBic(AGENT_BANK_PARTY_ID)).thenReturn(BANK11_BIC);
        CreatedEvent createdEvent = makeCreatedEvent(new ApprovedTransferProposal(
                AGENT_BANK_PARTY_ID,
                ASSEMBLER_PARTY_ID,
                Instant.EPOCH,
                Instant.EPOCH,
                Optional.ofNullable(reason),
                new SettlementStep(new SenderAndReceiver(SENDER_IBAN, RECEIVER_IBAN), new Instrument(BigDecimal.ONE, USD)),
                List.of(),
                PAYLOAD,
                MESSAGE_ID,
                GROUP_ID));

        // WHEN
        ApproveRejectProposal result = conversion.createdEventToKafka(createdEvent);

        // THEN
        MatcherAssert.assertThat(result.getGroupId(), Matchers.is(GROUP_ID));
        MatcherAssert.assertThat(result.getMessageId(), Matchers.is(MESSAGE_ID));
        MatcherAssert.assertThat(result.getReason(), Matchers.is(reason));
        MatcherAssert.assertThat(result.getStatus(), Matchers.is(Status.APPROVE));
        MatcherAssert.assertThat(result.getBankBic(), Matchers.is(BANK11_BIC));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {REASON})
    void GIVEN_contractId_to_proposal_key_mapping_exist_and_daml_reject_proposal_with_reason_WHEN_convert_THEN_convert_correctly(String reason) {
        // GIVEN
        Mockito.when(partyManager.getBic(AGENT_BANK_PARTY_ID)).thenReturn(BANK11_BIC);
        CreatedEvent createdEvent = makeCreatedEvent(new RejectedTransferProposal(
                AGENT_BANK_PARTY_ID,
                ASSEMBLER_PARTY_ID,
                Instant.EPOCH,
                Instant.EPOCH,
                Optional.ofNullable(reason),
                new SettlementStep(new SenderAndReceiver(SENDER_IBAN, RECEIVER_IBAN), new Instrument(BigDecimal.ONE, USD)),
                List.of(),
                PAYLOAD,
                MESSAGE_ID,
                GROUP_ID));

        // WHEN
        ApproveRejectProposal result = conversion.createdEventToKafka(createdEvent);

        // THEN
        MatcherAssert.assertThat(result.getGroupId(), Matchers.is(GROUP_ID));
        MatcherAssert.assertThat(result.getMessageId(), Matchers.is(MESSAGE_ID));
        MatcherAssert.assertThat(result.getReason(), Matchers.is(reason));
        MatcherAssert.assertThat(result.getStatus(), Matchers.is(Status.REJECT));
        MatcherAssert.assertThat(result.getBankBic(), Matchers.is(BANK11_BIC));
    }

    private static <T extends Template> CreatedEvent makeCreatedEvent(T contract) {
        return new CreatedEvent(
                List.of(),
                "",
                contract.getContractTypeId(),
                "",
                contract.toValue(),
                Map.of(),
                Map.of(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of()
        );
    }
}
