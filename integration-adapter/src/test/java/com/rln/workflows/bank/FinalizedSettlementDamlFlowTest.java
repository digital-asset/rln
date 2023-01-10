/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.bank;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.CommonBaseTest;
import com.rln.LedgerBaseTest;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.client.damlClient.listeners.exercise.FinalizeRejectSettlementDamlListener;
import com.rln.client.damlClient.partyManagement.ShardPartyPlainTextListReader;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.workflow.transactionmanifest.FinalizeSettlement;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.profile.TestWithBankModeProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

@TestProfile(TestWithBankModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class FinalizedSettlementDamlFlowTest extends LedgerBaseTest {

    private static final BiFunction<String, FinalizeSettlement, Boolean> CONTRACT_COMPARATOR = CommonBaseTest::equals;
    private static ExerciseCommand exerciseFinalizeSettlementCommand1;
    private static ExerciseCommand exerciseFinalizeSettlementCommand2;

    private static FinalizeSettlement[] finalizeSettlements;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    FinalizeRejectSettlementDamlListener finalizeRejectSettlementDamlListener;

    @BeforeAll
    static void setup() throws IOException, InterruptedException, TimeoutException {
        var legs1 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID), BIC_TO_PARTY_MAP);
        var legs2 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID2), BIC_TO_PARTY_MAP);
        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID, legs1);
        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID2, legs2);

        TransactionManifest.ContractId transferManifest1 = SANDBOX.getLedgerAdapter().getCreatedContractId(getSchedulerPartyId(), TransactionManifest.TEMPLATE_ID, TransactionManifest.ContractId::new);
        TransactionManifest.ContractId transferManifest2 = SANDBOX.getLedgerAdapter().getCreatedContractId(getSchedulerPartyId(), TransactionManifest.TEMPLATE_ID, TransactionManifest.ContractId::new);

        exerciseFinalizeSettlementCommand1 = transferManifest1.exerciseFinalizeSettlement(Optional.of(REASON));
        exerciseFinalizeSettlementCommand2 = transferManifest2.exerciseFinalizeSettlement(Optional.of(REASON));

        finalizeSettlements = new FinalizeSettlement[]{
                new FinalizeSettlement(Optional.of(REASON))
        };

        ShardPartyPlainTextListReader reader = Mockito.mock(ShardPartyPlainTextListReader.class);
        TransactionManifestCache transactionManifestCache = Mockito.mock(TransactionManifestCache.class);

        Mockito.when((transactionManifestCache.readFromValueToKey(ArgumentMatchers.any()))).thenReturn(GROUP_ID);
        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getBank11PartyId().getValue()));

        QuarkusMock.installMockForType(reader, ShardPartyPlainTextListReader.class, NamedLiteral.of(IAConstants.BANK_SHARD_PARTY_READER));
        QuarkusMock.installMockForType(transactionManifestCache, TransactionManifestCache.class);
    }

    @Test
    void WHEN_finalize_settlement_exercised_on_ledger_THEN_message_publish_on_kafka() throws InterruptedException, InvalidProtocolBufferException {
        SANDBOX.getLedgerAdapter().exerciseChoice(getAssemblerPartyId(),exerciseFinalizeSettlementCommand1);
        SANDBOX.getLedgerAdapter().exerciseChoice(getAssemblerPartyId(),exerciseFinalizeSettlementCommand2);
        eventually(()->kafkaAwaitCompletion(companion, TestWithBankModeProfile.FINALIZED_SETTLEMENT_OUTPUT_TOPIC, finalizeSettlements, CONTRACT_COMPARATOR));
    }
}
