/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.assembler;

import com.rln.client.damlClient.TestUtils;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.CommonBaseTest;
import com.rln.LedgerBaseTest;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.damlClient.listeners.exercise.ApproveRejectProposalDamlListener;
import com.rln.client.damlClient.partyManagement.ShardPartyPlainTextListReader;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.workflow.transferproposal.ApproveProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
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
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;

@TestProfile(TestWithAssemblerModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)

class ApproveRejectDamlFlowTest extends LedgerBaseTest {

    private static final BiFunction<String, ApproveProposal, Boolean> CONTRACT_COMPARATOR = CommonBaseTest::equals;
    private static ExerciseCommand exerciseApproveProposalsCommand1;
    private static ExerciseCommand exerciseApproveProposalsCommand2;

    private static final TransferProposalKey key = new TransferProposalKey(GROUP_ID, MESSAGE_ID, BANK11_BIC);

    private static final ApproveProposal[] APPROVE_PROPOSALS = new ApproveProposal[]{
            new ApproveProposal(Optional.of(REASON), false),
            new ApproveProposal(Optional.of(REASON2), false)
    };

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    ApproveRejectProposalDamlListener approveRejectProposalDamlListener;

    @BeforeAll
    static void setup() throws IOException {
        var legs1 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID), BIC_TO_PARTY_MAP);
        var legs2 = convertKafkaMessagesToMessageIdToLegList(getMessageIdWithStepsAndPayload(MESSAGE_ID2), BIC_TO_PARTY_MAP);

        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID, legs1);
        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID2, legs2);

        TransferProposal.ContractId proposalCid1 = SANDBOX.getLedgerAdapter().getCreatedContractId(
                getBank11PartyId(),
                TransferProposal.TEMPLATE_ID,
                TransferProposal.ContractId::new);
        TransferProposal.ContractId proposalCid2 = SANDBOX.getLedgerAdapter().getCreatedContractId(
                getBank11PartyId(),
                TransferProposal.TEMPLATE_ID,
                TransferProposal.ContractId::new);

        exerciseApproveProposalsCommand1 = TestUtils
            .toExerciseCommand(proposalCid1.exerciseApproveProposal(Optional.of(REASON), false));
        exerciseApproveProposalsCommand2 = TestUtils
            .toExerciseCommand(proposalCid2.exerciseApproveProposal(Optional.of(REASON2), false));

        TransferProposalCache cache = Mockito.mock(TransferProposalCache.class);
        ShardPartyPlainTextListReader reader = Mockito.mock(ShardPartyPlainTextListReader.class);

        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getAssemblerPartyId().getValue()));
        Mockito.when(cache.readFromValueToKey(proposalCid1)).thenReturn(ApproveRejectDamlFlowTest.key);
        Mockito.when(cache.readFromValueToKey(proposalCid2)).thenReturn(ApproveRejectDamlFlowTest.key);

        QuarkusMock.installMockForType(reader, ShardPartyPlainTextListReader.class, NamedLiteral.of(IAConstants.ASSEMBLER_SHARD_PARTY_READER));
        QuarkusMock.installMockForType(cache, TransferProposalCache.class);
    }

    @Test
    void WHEN_multiple_approve_proposals_exercised_on_ledger_THEN_multiple_kafka_messages_published() throws InterruptedException, InvalidProtocolBufferException {
        SANDBOX.getLedgerAdapter().exerciseChoice(getBank11PartyId(), exerciseApproveProposalsCommand1);
        SANDBOX.getLedgerAdapter().exerciseChoice(getBank11PartyId(), exerciseApproveProposalsCommand2);
        eventually(() -> kafkaAwaitCompletion(companion, TestWithAssemblerModeProfile.APPROVE_REJECT_PROPOSAL_OUTPUT_TOPIC, APPROVE_PROPOSALS, CONTRACT_COMPARATOR));
    }
}
