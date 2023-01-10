/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.bank;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.CommonBaseTest;
import com.rln.LedgerBaseTest;
import com.rln.client.damlClient.listeners.creation.ProposalDamlListener;
import com.rln.client.damlClient.partyManagement.NoSuchPartyException;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.damlClient.partyManagement.ShardPartyPlainTextListReader;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
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
import org.mockito.Mockito;

import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.function.BiFunction;

@TestProfile(TestWithBankModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class TransferProposalDamlFlowTest extends LedgerBaseTest {

    private static final BiFunction<String, TransferProposal, Boolean> CONTRACT_COMPARATOR = CommonBaseTest::equals;
    private static ExerciseCommand exerciseCreateProposalsCommand1;
    private static ExerciseCommand exerciseCreateProposalsCommand2;
    private static TransferProposal[] transferProposals;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    ProposalDamlListener proposalDamlListener;

    @BeforeAll
    static void setup() throws NoSuchPartyException, IOException {
        transferProposals = new TransferProposal[]{
                new TransferProposal(getBank11PartyId().getValue(), getSchedulerPartyId().getValue(), getAssemblerPartyId().getValue(), Instant.EPOCH,null, null, PAYLOAD, MESSAGE_ID, GROUP_ID),
                new TransferProposal(getBank11PartyId().getValue(), getSchedulerPartyId().getValue(), getAssemblerPartyId().getValue(), Instant.EPOCH,null, null, PAYLOAD, MESSAGE_ID2, GROUP_ID2),
        };
        var legs1 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID), BIC_TO_PARTY_MAP);
        var legs2 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID2), BIC_TO_PARTY_MAP);

        exerciseCreateProposalsCommand1 = prepareLedgerAndGetCreateProposalCommand(getBank11PartyId(), GROUP_ID, legs1);
        exerciseCreateProposalsCommand2 = prepareLedgerAndGetCreateProposalCommand(getBank11PartyId(), GROUP_ID2, legs2);

        ShardPartyPlainTextListReader reader = Mockito.mock(ShardPartyPlainTextListReader.class);
        PartyManager partyManager = Mockito.mock(PartyManager.class);
        Mockito.when(partyManager.hasPartyId(getBank11PartyId().getValue())).thenReturn(true);
        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getBank11PartyId().getValue()));
        QuarkusMock.installMockForType(reader, ShardPartyPlainTextListReader.class, NamedLiteral.of(IAConstants.BANK_SHARD_PARTY_READER));
        QuarkusMock.installMockForType(partyManager, PartyManager.class);
    }

    @Test
    void WHEN_multiple_transaction_proposals_created_on_ledger_THEN_multiple_kafka_messages_publish() throws InvalidProtocolBufferException, InterruptedException {
        SANDBOX.getLedgerAdapter().exerciseChoice(getSchedulerPartyId(), exerciseCreateProposalsCommand1);
        SANDBOX.getLedgerAdapter().exerciseChoice(getSchedulerPartyId(), exerciseCreateProposalsCommand2);

        eventually(() -> kafkaAwaitCompletion(
                companion,
                TestWithBankModeProfile.TRANSFER_PROPOSAL_OUTPUT_TOPIC,
                transferProposals,
                CONTRACT_COMPARATOR));
    }

}
