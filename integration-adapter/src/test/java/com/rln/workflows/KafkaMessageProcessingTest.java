/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rln.CommonBaseTest;
import com.rln.client.kafkaClient.message.InitiateTransfer;
import com.rln.messageprocessing.MessageProcessor;
import com.rln.profile.TestWithBankModeProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

@TestProfile(TestWithBankModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class KafkaMessageProcessingTest extends CommonBaseTest {
    public static final InitiateTransfer INITIATE_TRANSFER = new InitiateTransfer(GROUP_ID, BANK11_BIC, PAYLOAD);

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @InjectMock
    MessageProcessor<InitiateTransfer> messageProcessor;

    @Test
    void processorIsCalled() throws JsonProcessingException {
        int numberOfRecords = 3;
        var records = getRecordsForTopic(numberOfRecords, TestWithBankModeProfile.INITIATION_INPUT_TOPIC);

        companion.produceStrings().fromRecords(records);

        Mockito
                .verify(messageProcessor, Mockito.timeout(TIMEOUT).times(numberOfRecords))
                .accept(ArgumentMatchers.eq(INITIATE_TRANSFER));
    }

    private List<ProducerRecord<String, String>> getRecordsForTopic(int numberOfRecords, String topic) throws JsonProcessingException {
        var payload = jsonMapper.writeValueAsString(INITIATE_TRANSFER);
        return Collections.nCopies(numberOfRecords, new ProducerRecord<>(topic, payload));
    }
}
