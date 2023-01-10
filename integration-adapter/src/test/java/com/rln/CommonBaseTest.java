/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln;

import com.daml.ledger.javaapi.data.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.fields.MessageIdWithStepsAndPayload;
import com.rln.client.kafkaClient.message.fields.MessageWithBics;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Instrument;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.damlCodegen.workflow.transactionmanifest.FinalizeSettlement;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.damlCodegen.workflow.transferproposal.ApproveProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommonBaseTest {

    protected static final long TIMEOUT = 5000;

    protected static final String USD = "USD";
    protected static final String EUR = "EUR";

    protected static final String INITIATOR_PARTY_ID = "INITIATOR_PARTY_ID";
    protected static final String AGENT_BANK_PARTY_ID = "AGENT_BANK_PARTY";
    protected static final String AGENT_BANK_PARTY_ID2 = "AGENT_BANK_PARTY2";
    protected static final String ASSEMBLER_PARTY_ID = "ASSEMBLER_PARTY_ID";
    protected static final String SCHEDULER_PARTY_ID = "SCHEDULER_PARTY_ID";

    // BIC aligned with DAML scripts
    protected static final String BANK11_BIC = "BANK11_BIC";
    protected static final String BANK22_BIC = "BANK22_BIC";
    protected static final String BANK12_BIC = "BANK12_BIC";
    protected static final String BANK21_BIC = "BANK21_BIC";
    protected static final String BANKCB_BIC = "BANKCB_BIC";

    protected static final String SENDER_IBAN = "SENDER_IBAN";
    protected static final String RECEIVER_IBAN = "RECEIVER_IBAN";

    protected static final String GROUP_ID = "GROUP_ID";
    protected static final String GROUP_ID2 = "GROUP_ID2";
    protected static final String GROUP_ID3 = "GROUP_ID3";
    protected static final String GROUP_ID4 = "GROUP_ID4";
    protected static final String MESSAGE_ID = "MESSAGE_ID";
    protected static final String MESSAGE_ID2 = "MESSAGE_ID2";
    protected static final String MESSAGE_ID3 = "MESSAGE_ID3";
    protected static final String MESSAGE_ID4 = "MESSAGE_ID4";
    protected static final String MESSAGE_ID5 = "MESSAGE_ID5";
    protected static final String PAYLOAD = "PAYLOAD";

    protected static final String CONTRACT_ID = "CONTRACT_ID";
    protected static final String CONTRACT_ID_2 = "CONTRACT_ID_2";
    protected static final InitiateTransfer.ContractId INITIATE_TRANSFER_CONTRACT_ID = new InitiateTransfer.ContractId(CONTRACT_ID);
    protected static final String REASON = "Some reason";
    protected static final String REASON2 = "Another reason";

    protected static final String DAML_EVENT_ID = "DAML_EVENT_ID";
    // align with RLNDamlClient
    protected static final String DAML_WORK_FLOW_ID = "RLN";
    protected static final String DAML_TRANSACTION_ID = "DAML_TRANSACTION_ID";
    protected static final String DAML_RANDOM_COMMAND_ID = "DAML_RANDOM_COMMAND_ID";

    protected static final Identifier DUMMY_IDENTIFIER = new Identifier("", "", "");

    protected static final ObjectMapper jsonMapper = new ObjectMapper();
    protected static final PartyManager partyManager = Mockito.mock(PartyManager.class);
    @SneakyThrows
    protected static boolean equals(String kafkaInitiateTransferStr, InitiateTransfer damlInitiateTransfer) {
        com.rln.client.kafkaClient.message.InitiateTransfer kafkaInitiateTransfer = jsonMapper.readValue(kafkaInitiateTransferStr, com.rln.client.kafkaClient.message.InitiateTransfer.class);
        return kafkaInitiateTransfer.getInitiator().equals(BANK11_BIC) &&
                kafkaInitiateTransfer.getGroupId().equals(damlInitiateTransfer.groupId) &&
                kafkaInitiateTransfer.getPayload().equals(damlInitiateTransfer.payload);
    }

    @SneakyThrows
    protected static boolean equals(String kafkaInitiateTransferStr, com.rln.client.kafkaClient.message.InitiateTransfer kafkaInitiateTransfer) {
        return kafkaInitiateTransfer.equals(jsonMapper.readValue(kafkaInitiateTransferStr, com.rln.client.kafkaClient.message.InitiateTransfer.class));
    }

    @SneakyThrows
    protected static boolean equals(String kafkaTransferProposalStr, TransferProposal damlTransferProposal) {
        com.rln.client.kafkaClient.message.TransferProposal kafkaTransferProposal = jsonMapper.readValue(kafkaTransferProposalStr, com.rln.client.kafkaClient.message.TransferProposal.class);
        return kafkaTransferProposal.getMessageId().equals(damlTransferProposal.messageId) &&
                kafkaTransferProposal.getGroupId().equals(damlTransferProposal.groupId) &&
                kafkaTransferProposal.getPayload().equals(damlTransferProposal.legPayload);
    }

    @SneakyThrows
    protected static boolean equals(String kafkaFinalizeSettlementStr, FinalizeSettlement damlFinalizeSettlement) {
        com.rln.client.kafkaClient.message.FinalizeRejectSettlement kafkaFinalizeSettlement = jsonMapper.readValue(kafkaFinalizeSettlementStr, com.rln.client.kafkaClient.message.FinalizeRejectSettlement.class);
        return kafkaFinalizeSettlement.getReason().equals(damlFinalizeSettlement.reason.orElse(IAConstants.JSON_NULL_STRING));
    }

    @SneakyThrows
    protected static boolean equals(String kafkaTransactionManifestStr, TransactionManifest transactionManifest) {
        com.rln.client.kafkaClient.message.TransactionManifest kafkaTransactionManifest = jsonMapper.readValue(kafkaTransactionManifestStr, com.rln.client.kafkaClient.message.TransactionManifest.class);
        return transactionManifest.groupId.equals(kafkaTransactionManifest.getGroupId()) &&
                equals(kafkaTransactionManifest.getMessageWithBics(), transactionManifest.messageIdToApprovers);
    }

    @SneakyThrows
    protected static boolean equals(String kafkaApproveProposalStr, ApproveProposal approveProposal) {
        com.rln.client.kafkaClient.message.ApproveRejectProposal kafkaApproveProposal = jsonMapper.readValue(kafkaApproveProposalStr, com.rln.client.kafkaClient.message.ApproveRejectProposal.class);
        return kafkaApproveProposal.getReason().equals(approveProposal.reason.orElse(IAConstants.JSON_NULL_STRING));
    }

    protected static boolean equals(MessageWithBics[] messageWithBicsArr, List<Tuple2<String, List<String>>> messageIdToApprovers) {
        for (MessageWithBics messageWithBics : messageWithBicsArr) {
            String messageId = messageWithBics.getMessageId();
            List<String> targetPartyIds = Arrays.asList(messageWithBics.getBics());
            List<String> matchedPartyIds = messageIdToApprovers.stream()
                    .filter(messageIdToParties -> messageIdToParties._1.equals(messageId)).findFirst()
                    .map(t -> t._2.stream().map(partyManager::getBic).collect(Collectors.toList()))
                    .orElseThrow();
            if (!matchedPartyIds.equals(targetPartyIds)) {
                return false;
            }
        }
        return true;
    }

    protected static TransactionTree createTransactionTreeFromEvent(Map<String, TreeEvent> events) {
        return new TransactionTree(DAML_TRANSACTION_ID, DAML_RANDOM_COMMAND_ID, DAML_WORK_FLOW_ID,
                Instant.now(), events, List.copyOf(events.keySet()), "0");
    }

    protected static List<ProducerRecord<String, String>> generateJsonStrRecords(Object[] payloads, String topic) throws JsonProcessingException {
        List<ProducerRecord<String, String>> records = new ArrayList<>();
        for (Object payload : payloads) {
            String kafkaProducerPayload = jsonMapper.writeValueAsString(payload);
            records.add(new ProducerRecord<>(topic, kafkaProducerPayload));
        }
        return records;
    }

    protected static CreatedEvent getCreatedEvent(Identifier identifier) {
        var createdEvent = Mockito.mock(CreatedEvent.class);
        Mockito.when(createdEvent.getTemplateId()).thenReturn(identifier);
        return createdEvent;
    }

    protected static ExercisedEvent getExercisedEvent(Identifier identifier) {
        var exerciseEvent = Mockito.mock(ExercisedEvent.class);
        Mockito.when(exerciseEvent.getTemplateId()).thenReturn(identifier);
        return exerciseEvent;
    }

    protected static void produceMessageOnKafka(KafkaCompanion companion, String topic, Object[] payloads) throws IOException {
        List<ProducerRecord<String, String>> records = generateJsonStrRecords(payloads, topic);
        companion.produceStrings().fromRecords(records);
    }

    protected static List<Tuple2<String, Leg>> convertKafkaMessagesToMessageIdToLegList(MessageIdWithStepsAndPayload[] messageIdWithStepsAndPayloads, Map<String, Party> bicToPartyMap) {
        return Arrays.stream(messageIdWithStepsAndPayloads).map(messageIdWithStepsAndPayload -> {
            List<Tuple2<String, SettlementStep>> translated = Arrays.stream(messageIdWithStepsAndPayload.getSteps())
                .map(step ->
                    new Tuple2<String, SettlementStep>(
                        bicToPartyMap.get(step.getApprover()).getValue(),
                        new SettlementStep(Optional.ofNullable(step.getSender()), Optional.ofNullable(step.getReceiver()),
                            new Instrument(BigDecimal.valueOf(step.getAmount()), step.getLabel()))))
                .collect(Collectors.toList());
            Leg leg = new Leg(messageIdWithStepsAndPayload.getPayload(), translated);
            return new Tuple2<>(messageIdWithStepsAndPayload.getMessageId(), leg);
        }).collect(Collectors.toList());
    }

    protected static <T> void kafkaAwaitCompletion(KafkaCompanion companion, String topic, T[] contracts, BiFunction<String,T,Boolean> isEqual){
        Assertions.assertTrue(contracts.length > 0);

        var consumer = companion
                .consumeStrings()
                .withAutoCommit()
                .fromTopics(topic, contracts.length);

        consumer.awaitCompletion();

        for (T contract: contracts) {
            boolean matchFound =
                    consumer.getRecords()
                            .stream().map(ConsumerRecord::value)
                            .anyMatch(v -> isEqual.apply(v, contract));
            Assertions.assertTrue(matchFound);
        }
    }


    protected void eventually(Runnable code) throws InterruptedException {
        Instant started = Instant.now();
        Function<Duration, Boolean> hasPassed =
                x -> Duration.between(started, Instant.now()).compareTo(x) > 0;
        boolean isSuccessful = false;
        while (!isSuccessful) {
            try {
                code.run();
                isSuccessful = true;
            } catch (Throwable ignore) {
                if (hasPassed.apply(Duration.ofSeconds(30))) {
                    Assertions.fail("Code did not succeed in time.");
                } else {
                    Thread.sleep(200);
                    isSuccessful = false;
                }
            }
        }
    }
}
