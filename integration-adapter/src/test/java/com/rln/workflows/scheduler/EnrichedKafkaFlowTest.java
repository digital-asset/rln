/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.scheduler;

import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.Party;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.LedgerBaseTest;
import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.cache.PartyCache.SchedulerPartyCache;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.damlClient.partyManagement.ShardPartyReader;
import com.rln.client.kafkaClient.message.EnrichedPacs008;
import com.rln.client.kafkaClient.message.fields.MessageIdWithStepsAndPayload;
import com.rln.client.kafkaClient.message.fields.Step;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Instrument;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import com.rln.damlCodegen.workflow.data.ibans.SenderAndReceiver;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import com.rln.profile.TestWithSchedulerModeProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@TestProfile(TestWithSchedulerModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class EnrichedKafkaFlowTest extends LedgerBaseTest {

    Map<String, MessageIdWithStepsAndPayload[]> oneStepMessage = Map.of(GROUP_ID, MESSAGES_1);
    Map<String, MessageIdWithStepsAndPayload[]> twoStepMessage = Map.of(GROUP_ID2, MESSAGES_2);
    Map<String, MessageIdWithStepsAndPayload[]> multipleMessages = Map.of(GROUP_ID3, MESSAGES_1, GROUP_ID4, MESSAGES_2);

    private static InitiateTransferCache.InitiatorAndContractId initiateTransfer1;
    private static InitiateTransferCache.InitiatorAndContractId initiateTransfer2;
    private static InitiateTransferCache.InitiatorAndContractId initiateTransfer3;
    private static InitiateTransferCache.InitiatorAndContractId initiateTransfer4;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @InjectMock
    @Named(IAConstants.ASSEMBLER_SHARD_PARTY_READER)
    ShardPartyReader reader;

    @InjectMock(convertScopes = true)
    PartyManager partyManager;

    @InjectMock
    InitiateTransferCache initiateTransferContractIdCache;

    @InjectMock
    SchedulerPartyCache schedulerPartyCache;

    @BeforeAll
    public static void publishInitiateTransferToLedger() throws InvalidProtocolBufferException {
        initiateTransfer1 = createInitiateTransferContractOnLedger(getBank11PartyId(), getInitiateTransferRecord(GROUP_ID));
        initiateTransfer2 = createInitiateTransferContractOnLedger(getBank11PartyId(), getInitiateTransferRecord(GROUP_ID2));
        initiateTransfer3 = createInitiateTransferContractOnLedger(getBank11PartyId(), getInitiateTransferRecord(GROUP_ID3));
        initiateTransfer4 = createInitiateTransferContractOnLedger(getBank11PartyId(), getInitiateTransferRecord(GROUP_ID4));
    }

    @BeforeEach
    public void setCustomsMocks() {
        setCommonPartyManagerMocks(); // need it here explicitly for current partyManager that I use to convertToMessageIdToLegs
        Mockito.when(schedulerPartyCache.read(ArgumentMatchers.anyString())).thenReturn(getSchedulerPartyId().getValue());
        Mockito.when(initiateTransferContractIdCache.read(GROUP_ID)).thenReturn(initiateTransfer1);
        Mockito.when(initiateTransferContractIdCache.read(GROUP_ID2)).thenReturn(initiateTransfer2);
        Mockito.when(initiateTransferContractIdCache.read(GROUP_ID3)).thenReturn(initiateTransfer3);
        Mockito.when(initiateTransferContractIdCache.read(GROUP_ID4)).thenReturn(initiateTransfer4);
        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getAssemblerPartyId().getValue()));
    }

    @Test
    void WHEN_enriched_pacs008_message_publish_to_kafka_THEN_transfer_proposals_and_transfer_manifest_created_on_ledger() throws IOException {
        EnrichedPacs008[] kafkaProducerPayload = createEnrichedMessagePayloads(oneStepMessage);
        produceMessageOnKafka(companion, TestWithSchedulerModeProfile.ENRICHED_PAC008_INPUT_TOPIC, kafkaProducerPayload);
        lookUpTransferProposals(getTransferProposalMatchers(kafkaProducerPayload));
        lookUpTransactionManifest(buildTransferManifestRecordsWithKafkaPayload(kafkaProducerPayload));
    }

    @Test
    void WHEN_enriched_pacs008_message_multiple_messageIDs_publish_to_kafka_THEN_transfer_proposals_created_on_ledger() throws IOException {
        EnrichedPacs008[] kafkaProducerPayload = createEnrichedMessagePayloads(twoStepMessage);
        produceMessageOnKafka(companion, TestWithSchedulerModeProfile.ENRICHED_PAC008_INPUT_TOPIC, kafkaProducerPayload);
        lookUpTransferProposals(getTransferProposalMatchers(kafkaProducerPayload));
        lookUpTransactionManifest(buildTransferManifestRecordsWithKafkaPayload(kafkaProducerPayload));
    }

    @Test
    void WHEN_multiple_enriched_pacs008_message_multiple_messageIDs_publish_to_kafka_THEN_transfer_proposals_created_on_ledger() throws IOException {
        EnrichedPacs008[] kafkaProducerPayload = createEnrichedMessagePayloads(multipleMessages);
        produceMessageOnKafka(companion, TestWithSchedulerModeProfile.ENRICHED_PAC008_INPUT_TOPIC, kafkaProducerPayload);
        lookUpTransferProposals(getTransferProposalMatchers(kafkaProducerPayload));
        lookUpTransactionManifest(buildTransferManifestRecordsWithKafkaPayload(kafkaProducerPayload));
    }

    private void lookUpTransactionManifest(List<DamlRecord> matchers){
        lookUpContractWithMatcher(
                TransactionManifest.TEMPLATE_ID,
                TransactionManifest.ContractId::new,
                matchers,
          getSchedulerPartyId());
    }

    private void lookUpTransferProposals(List<DamlRecord> matchers){
        lookUpContractWithMatcher(
                TransferProposal.TEMPLATE_ID,
                TransferProposal.ContractId::new,
                matchers,
          getSchedulerPartyId());
    }

    private List<DamlRecord> getTransferProposalMatchers(EnrichedPacs008[] kafkaProducerPayloads){
        List<DamlRecord> matchers = new ArrayList<>();
        for(EnrichedPacs008 kafkaProducerPayload : kafkaProducerPayloads){
            for(MessageIdWithStepsAndPayload message: kafkaProducerPayload.getMessages()){
                for(Step step: message.getSteps()){
                    matchers.add(buildTransferProposalRecordMatcher(BIC_TO_PARTY_MAP.get(step.getApprover()), step.getSender(), step.getReceiver(), BigDecimal.valueOf(step.getAmount()).setScale(10), step.getLabel(), message.getMessageId(), kafkaProducerPayload.getGroupId()));
                }
            }

        }
        return matchers;
    }

    private List<DamlRecord> buildTransferManifestRecordsWithKafkaPayload(EnrichedPacs008[] kafkaPayloads) {
        return Arrays.stream(kafkaPayloads).map( kafkaPayload -> {
            List<Tuple2<String, List<String>>> messageIdToApprovers = buildMessageIdToApproversFieldValueFromPayloadMessages(kafkaPayload.getMessages());
            return new TransactionManifest(getAssemblerPartyId().getValue(), getSchedulerPartyId().getValue(),
                kafkaPayload.getGroupId(), messageIdToApprovers, Instant.EPOCH).toValue();
        }).collect(Collectors.toList());
    }

    private List<Tuple2<String, List<String>>> buildMessageIdToApproversFieldValueFromPayloadMessages(MessageIdWithStepsAndPayload[] messages) {
        return Arrays.stream(messages).map(message -> {
            Party[] listOfApprovers = translateBicsInStepsToParties(message.getSteps());
            return new Tuple2<>(message.getMessageId(), Arrays.stream(listOfApprovers).map(Party::getValue).collect(Collectors.toList()));
        }).collect(Collectors.toList());
    }

    private DamlRecord buildTransferProposalRecordMatcher(Party ownerParty, String sender, String receiver, BigDecimal amount, String label, String messageID, String groupId) {
        List<String> listOfApproverParties = List.of(ownerParty.getValue());
        SettlementStep settlementStep = new SettlementStep(new SenderAndReceiver(sender, receiver), new Instrument(amount, label));
        return new TransferProposal(ownerParty.getValue(), getSchedulerPartyId().getValue(), getAssemblerPartyId().getValue(), Instant.EPOCH, settlementStep, listOfApproverParties, PAYLOAD, messageID, groupId).toValue();
    }

    private EnrichedPacs008[] createEnrichedMessagePayloads(
            Map<String, MessageIdWithStepsAndPayload[]> messageGroupIdPairs) {
        return messageGroupIdPairs.entrySet().stream().map(messageGroupIdPair ->{
            String groupId = messageGroupIdPair.getKey();
            MessageIdWithStepsAndPayload[] message =  messageGroupIdPair.getValue();
            return new EnrichedPacs008(groupId, message);
        }).toArray(EnrichedPacs008[]::new);
    }
}
