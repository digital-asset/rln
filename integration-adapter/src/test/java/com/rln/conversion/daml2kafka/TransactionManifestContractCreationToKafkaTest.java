/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.daml2kafka;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.CommonBaseTest;
import com.rln.client.damlClient.partyManagement.NoSuchPartyException;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

class TransactionManifestContractCreationToKafkaTest extends CommonBaseTest {

    private static final TransactionManifestContractCreationToKafka conversion =
            new TransactionManifestContractCreationToKafka(partyManager);
    private static final CreatedEvent createdEvent = getCreatedEvent(TransactionManifest.TEMPLATE_ID);


    @Test
    void GIVEN_party_to_bic_mapping_WHEN_convert_THEN_create_kafka_pojo_correctly() {
        // GIVEN
        Mockito.when(partyManager.getBic(INITIATOR_PARTY_ID)).thenReturn(BANK11_BIC);
        Mockito.when(partyManager.getBic(AGENT_BANK_PARTY_ID)).thenReturn(BANK22_BIC);
        Mockito.when(partyManager.getBic(AGENT_BANK_PARTY_ID2)).thenReturn(BANK12_BIC);

        // WHEN
        List<Tuple2<String, List<String>>> messageIdToApprovers = List.of(
                new Tuple2<>(MESSAGE_ID, List.of(INITIATOR_PARTY_ID, AGENT_BANK_PARTY_ID)),
                new Tuple2<>(MESSAGE_ID2, List.of(AGENT_BANK_PARTY_ID2)));
        TransactionManifest transactionManifest = new TransactionManifest(ASSEMBLER_PARTY_ID, SCHEDULER_PARTY_ID, GROUP_ID, messageIdToApprovers, Instant.EPOCH);
        Mockito.when(createdEvent.getArguments()).thenReturn(transactionManifest.toValue());

        com.rln.client.kafkaClient.message.TransactionManifest result = conversion.createdEventToKafka(createdEvent);

        // THEN
        MatcherAssert.assertThat(result.getGroupId(), Matchers.is(GROUP_ID));
        MatcherAssert.assertThat(equals(result.getMessageWithBics(), transactionManifest.messageIdToApprovers), Matchers.is(true));
    }

    @Test
    void GIVEN_party_to_bic_mapping_does_not_exist_WHEN_convert_THEN_rethrow_exception_from_getBic() {
        // GIVEN
        String nonExistingParty = "nonExistingParty";
        Mockito.when(partyManager.getBic(nonExistingParty)).thenThrow(NoSuchPartyException.class);

        // WHEN
        List<Tuple2<String, List<String>>> messageIdToApprovers = List.of(
                new Tuple2<>(MESSAGE_ID, List.of(nonExistingParty, AGENT_BANK_PARTY_ID)));
        TransactionManifest transactionManifest = new TransactionManifest(ASSEMBLER_PARTY_ID, SCHEDULER_PARTY_ID, GROUP_ID, messageIdToApprovers, Instant.EPOCH);
        Mockito.when(createdEvent.getArguments()).thenReturn(transactionManifest.toValue());


        // THEN
        Assertions.assertThrows(NoSuchPartyException.class, () -> conversion.createdEventToKafka(createdEvent));
    }

    @Test
    void GIVEN_null_messageId_with_party_to_bic_mapping_WHEN_convert_THEN_IllegalArgumentException_throw() {
        // GIVEN
        Mockito.when(partyManager.getBic(INITIATOR_PARTY_ID)).thenReturn(BANK11_BIC);

        // WHEN
        List<Tuple2<String, List<String>>> messageIdToApprovers = List.of(new Tuple2<>(null, List.of(INITIATOR_PARTY_ID)));
        TransactionManifest transactionManifest = new TransactionManifest(ASSEMBLER_PARTY_ID, SCHEDULER_PARTY_ID, GROUP_ID, messageIdToApprovers, Instant.EPOCH);
        Mockito.when(createdEvent.getArguments()).thenReturn(transactionManifest.toValue());

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class, () -> conversion.createdEventToKafka(createdEvent));
    }

    @Test
    void GIVEN_empty_bics_WHEN_convert_THEN_conversion_succeed() {
        // GIVEN + WHEN
        List<Tuple2<String, List<String>>> messageIdToApprovers = List.of(new Tuple2<>(MESSAGE_ID, Collections.emptyList()));
        TransactionManifest transactionManifest = new TransactionManifest(ASSEMBLER_PARTY_ID, SCHEDULER_PARTY_ID, GROUP_ID, messageIdToApprovers, Instant.EPOCH);
        Mockito.when(createdEvent.getArguments()).thenReturn(transactionManifest.toValue());

        com.rln.client.kafkaClient.message.TransactionManifest result = conversion.createdEventToKafka(createdEvent);

        // THEN
        MatcherAssert.assertThat(result, Matchers.notNullValue());
    }
}
