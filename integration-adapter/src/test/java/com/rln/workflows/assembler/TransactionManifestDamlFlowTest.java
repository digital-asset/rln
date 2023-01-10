/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.assembler;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.CommonBaseTest;
import com.rln.LedgerBaseTest;
import com.rln.client.damlClient.listeners.creation.TransactionManifestDamlListener;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.damlClient.partyManagement.ShardPartyPlainTextListReader;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.profile.TestWithAssemblerModeProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@TestProfile(TestWithAssemblerModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)

class TransactionManifestDamlFlowTest extends LedgerBaseTest {

    private static final BiFunction<String, TransactionManifest, Boolean> CONTRACT_COMPARATOR = CommonBaseTest::equals;
    private static ExerciseCommand exerciseCreateProposalsCommand1;
    private static ExerciseCommand exerciseCreateProposalsCommand2;
    private static TransactionManifest[] transactionManifests;
    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    TransactionManifestDamlListener transactionManifestDamlListener;

    @BeforeAll
    static void setup() throws IOException {
        var legs1 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID), BIC_TO_PARTY_MAP);
        var legs2 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID2), BIC_TO_PARTY_MAP);

        var messageIdToApprovers1 = legs1
                .stream().map(TransactionManifestDamlFlowTest::messageIdToLegToMessageIdToApproves).collect(Collectors.toList());
        var messageIdToApprovers2 = legs2
            .stream().map(TransactionManifestDamlFlowTest::messageIdToLegToMessageIdToApproves).collect(Collectors.toList());

        transactionManifests = new TransactionManifest[]{
                new TransactionManifest(getAssemblerPartyId().getValue(), getSchedulerPartyId().getValue(), GROUP_ID, messageIdToApprovers1, Instant.EPOCH),
                new TransactionManifest(getAssemblerPartyId().getValue(), getSchedulerPartyId().getValue(), GROUP_ID2, messageIdToApprovers2, Instant.EPOCH)
        };

        exerciseCreateProposalsCommand1 = prepareLedgerAndGetCreateProposalCommand(getBank11PartyId(), GROUP_ID, legs1);
        exerciseCreateProposalsCommand2 = prepareLedgerAndGetCreateProposalCommand(getBank11PartyId(), GROUP_ID2, legs2);

        ShardPartyPlainTextListReader reader = Mockito.mock(ShardPartyPlainTextListReader.class);
        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getAssemblerPartyId().getValue()));

        QuarkusMock.installMockForType(partyManager, PartyManager.class);
        QuarkusMock.installMockForType(reader, ShardPartyPlainTextListReader.class, NamedLiteral.of(IAConstants.ASSEMBLER_SHARD_PARTY_READER));
    }

    private static Tuple2<String, List<String>> messageIdToLegToMessageIdToApproves(Tuple2<String, Leg> messageIdToLeg) {
        String messageId = messageIdToLeg._1;
        List<String> approvers = messageIdToLeg._2.approversToSettlementSteps.stream()
                .map(approversToSettlementStep -> approversToSettlementStep._1)
                .collect(Collectors.toList());
        return new Tuple2<>(messageId, approvers);
    }

    @Test
    void WHEN_multiple_transaction_manifests_created_on_ledger_THEN_multiple_kafka_messages_published() throws InvalidProtocolBufferException, InterruptedException {
        SANDBOX.getLedgerAdapter().exerciseChoice(getSchedulerPartyId(), exerciseCreateProposalsCommand1);
        SANDBOX.getLedgerAdapter().exerciseChoice(getSchedulerPartyId(), exerciseCreateProposalsCommand2);

        eventually(() -> kafkaAwaitCompletion(
                companion,
                TestWithAssemblerModeProfile.TRANSACTION_MANIFEST_OUTPUT_TOPIC,
                transactionManifests,
                CONTRACT_COMPARATOR));

    }
}
