/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.bank;

import com.daml.extensions.testing.Dsl;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.rln.LedgerBaseTest;
import com.rln.client.damlClient.partyManagement.ShardPartyReader;
import com.rln.client.kafkaClient.message.InitiateTransfer;
import com.rln.common.IAConstants;
import com.rln.profile.TestWithBankModeProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.Mockito;

import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@TestProfile(TestWithBankModeProfile.class)
@TestMethodOrder(OrderAnnotation.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class InitiationKafkaFlowTest extends LedgerBaseTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @InjectMock
    @Named(IAConstants.SCHEDULER_SHARD_PARTY_READER)
    ShardPartyReader reader;

    @BeforeEach
    public void setCustomsMocks() {
        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getSchedulerPartyId().getValue()));
    }

    @Order(1)
    @Test
    void WHEN_initiate_transfer_message_publish_to_kafka_THEN_initiate_transfer_contract_created_on_ledger() throws IOException {
        int numberOfRecords = 1;

        InitiateTransfer[] kafkaProducerPayload = createInitiateTransferPayloads(numberOfRecords);
        produceMessageOnKafka(companion, TestWithBankModeProfile.INITIATION_INPUT_TOPIC, kafkaProducerPayload);

        lookUpInitiateTransfer(getInitiationTransferRecordMatchers(kafkaProducerPayload));
    }

    @Order(2)
    @Test
    void WHEN_multiple_initiate_transfer_message_publish_to_kafka_THEN_initiate_transfer_contracts_created_on_ledger() throws IOException {
        int numberOfRecords = 3;

        InitiateTransfer[] kafkaProducerPayload = createInitiateTransferPayloads(numberOfRecords);
        produceMessageOnKafka(companion, TestWithBankModeProfile.INITIATION_INPUT_TOPIC, kafkaProducerPayload);

        lookUpInitiateTransfer(getInitiationTransferRecordMatchers(kafkaProducerPayload));
    }

    @Order(3)
    @Test
    void WHEN_new_message_received_after_exception_thrown_from_previous_message_THEN_new_message_processed_correctly() throws IOException {
        Mockito.when(reader.getShardParties())
                .thenThrow(new RuntimeException("Big error!"))
                .thenReturn(Collections.singletonList(getSchedulerPartyId().getValue()));

        int numberOfRecords = 1;
        InitiateTransfer[] kafkaProducerPayload = createInitiateTransferPayloads(numberOfRecords);
        produceMessageOnKafka(companion, TestWithBankModeProfile.INITIATION_INPUT_TOPIC, kafkaProducerPayload);
        // no message publish to kafka as exception has occurred while processing current message
        Assertions.assertThrows(
                TimeoutException.class,
                () -> {
                    SANDBOX.getLedgerAdapter().getCreatedContractId(
                            getBank11PartyId(),
                            com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.TEMPLATE_ID,
                            com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.ContractId::new);
                }
        );

        // new message coming after exception and no exception thrown when processing new message
        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getSchedulerPartyId().getValue()));
        kafkaProducerPayload = createInitiateTransferPayloads(numberOfRecords);
        produceMessageOnKafka(companion, TestWithBankModeProfile.INITIATION_INPUT_TOPIC, kafkaProducerPayload);
        // new message from kafka get translated to daml ledger correctly
        lookUpInitiateTransfer(getInitiationTransferRecordMatchers(kafkaProducerPayload));
    }

    private DamlRecord getInitiationTransferRecordMatcher(String payload) {
        return Dsl.record(
                Dsl.field(GID_FIELD_NAME, Dsl.text(GROUP_ID)),
                Dsl.field(INITIATOR_FIELD_NAME, getBank11PartyId()),
                Dsl.field(SCHEDULER_FIELD_NAME, getSchedulerPartyId()),
                Dsl.field(PAYLOAD_FIELD_NAME, Dsl.text(payload)));
    }

    private List<DamlRecord> getInitiationTransferRecordMatchers(InitiateTransfer[] kafkaProducerPayloads) {
        return Arrays.stream(kafkaProducerPayloads).map(kafkaProducerPayload ->{
            String payload = kafkaProducerPayload.getPayload();
            return getInitiationTransferRecordMatcher(payload);
        }).collect(Collectors.toList());
    }

    private void lookUpInitiateTransfer(List<DamlRecord> matchers){
        lookUpContractWithMatcher(
                com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.TEMPLATE_ID,
                com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.ContractId::new,
                matchers,
                getBank11PartyId());
    }

    private InitiateTransfer[] createInitiateTransferPayloads(int numberOfPayloads) {
        InitiateTransfer[] payloads = new InitiateTransfer[numberOfPayloads];
        Arrays.fill(
                payloads,
                new InitiateTransfer(
                    GROUP_ID,
                        BANK11_BIC,
                    UUID.randomUUID().toString())
        );
        return payloads;
    }
}
