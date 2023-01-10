/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows;

import com.rln.CommonBaseTest;
import com.rln.client.kafkaClient.message.InitiateTransfer;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.common.IAConstants;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;

@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class KafkaMessageSubmissionTest extends CommonBaseTest {
    // This has to be the same as the one in application.properties, using a profile does not help.
    private static final String INITIATION_TRANSFER_TOPIC = "initiation";

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    @Named(IAConstants.KAFKA_INITIATE_TRANSFER_SUBMITTER)
    KafkaSubmitter<InitiateTransfer> kafkaSubmitter;

    @Test
    void GIVEN_InitiateTransfer_WHEN_submit_with_kafka_submitter_THEN_record_published_on_kafka() {
        InitiateTransfer initiateTransfer = new InitiateTransfer(GROUP_ID, BANK11_BIC, PAYLOAD);

        kafkaSubmitter.submit(initiateTransfer);

        var consumer = companion.consumeStrings().withAutoCommit()
                .fromTopics(INITIATION_TRANSFER_TOPIC, 1);
        consumer.awaitCompletion();
        boolean matchedFound = consumer
                .getRecords().stream().map(ConsumerRecord::value)
                .anyMatch(v -> equals(v, initiateTransfer));
        MatcherAssert.assertThat(matchedFound, Matchers.is(true));
    }
}
