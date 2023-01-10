/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.assembler;

import com.daml.ledger.javaapi.data.ContractId;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.LedgerBaseTest;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.cache.PartyCache.AssemblerPartyCache;
import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.profile.TestWithAssemblerModeProfile;
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

import java.io.IOException;
import java.util.List;

@TestProfile(TestWithAssemblerModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class FinalizeRejectSettlementKafkaFlowTest extends LedgerBaseTest {

    private static ContractId manifestCid1;
    private static ContractId manifestCid2;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @InjectMock
    AssemblerPartyCache assemblerPartyCache;

    @InjectMock
    TransactionManifestCache transactionManifestCache;

    @BeforeAll
    public static void publishTransactionManifestToLedger() throws InvalidProtocolBufferException {
        List<Tuple2<String, Leg>> legs1 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID), BIC_TO_PARTY_MAP);
        List<Tuple2<String, Leg>> legs2 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID2), BIC_TO_PARTY_MAP);

        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID, legs1);
        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID2, legs2);

        manifestCid1 = SANDBOX.getLedgerAdapter().getCreatedContractId(
                getSchedulerPartyId(),
                TransactionManifest.TEMPLATE_ID,
                ContractId::new);
        manifestCid2 = SANDBOX.getLedgerAdapter().getCreatedContractId(
                getSchedulerPartyId(),
                TransactionManifest.TEMPLATE_ID,
                ContractId::new);
    }

    @BeforeEach
    public void setUpCustomMocks(){
        TransactionManifest.ContractId transferManifestGenCid1 = new TransactionManifest.ContractId(manifestCid1.getValue());
        TransactionManifest.ContractId transferManifestGenCid2 = new TransactionManifest.ContractId(manifestCid2.getValue());

        Mockito.when(assemblerPartyCache.read(ArgumentMatchers.any())).thenReturn(getAssemblerPartyId().getValue());
        Mockito.when(transactionManifestCache.readFromKeyToValue(GROUP_ID)).thenReturn(transferManifestGenCid1);
        Mockito.when(transactionManifestCache.readFromKeyToValue(GROUP_ID2)).thenReturn(transferManifestGenCid2);
    }

    @Test
    void WHEN_multiple_finalization_reject_messages_publish_to_kafka_THEN_multiple_manifests_archived() throws IOException {
        FinalizeRejectSettlement[] finalizeRejectSettlements = {
                new FinalizeRejectSettlement(GROUP_ID, Status.APPROVE, REASON),
                new FinalizeRejectSettlement(GROUP_ID2, Status.APPROVE, REASON)};
        produceMessageOnKafka(companion, TestWithAssemblerModeProfile.FINALIZE_REJECT_INPUT_TOPIC, finalizeRejectSettlements);
        testContractArchived(TransactionManifest.TEMPLATE_ID, getAssemblerPartyId(), manifestCid1);
        testContractArchived(TransactionManifest.TEMPLATE_ID, getAssemblerPartyId(), manifestCid2);
    }
}
