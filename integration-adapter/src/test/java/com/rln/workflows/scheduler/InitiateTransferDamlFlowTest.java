/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.scheduler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.CommonBaseTest;
import com.rln.LedgerBaseTest;
import com.rln.client.damlClient.listeners.creation.InitiationDamlListener;
import com.rln.client.damlClient.partyManagement.ShardPartyPlainTextListReader;
import com.rln.common.IAConstants;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.profile.TestWithSchedulerModeProfile;
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
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

@TestProfile(TestWithSchedulerModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class InitiateTransferDamlFlowTest extends LedgerBaseTest {

    private static final BiFunction<String, InitiateTransfer, Boolean> CONTRACT_COMPARATOR = CommonBaseTest::equals;
    private static InitiateTransfer[] initiateTransfers;

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Inject
    InitiationDamlListener initiationDamlListener;


    @BeforeAll
    protected static void setup() throws IOException, InterruptedException, TimeoutException {
        initiateTransfers = new InitiateTransfer[]{
                new InitiateTransfer(GROUP_ID, getBank11PartyId().getValue(), getSchedulerPartyId().getValue(), PAYLOAD),
                new InitiateTransfer(GROUP_ID2, getBank11PartyId().getValue(), getSchedulerPartyId().getValue(), PAYLOAD)
        };

        ShardPartyPlainTextListReader reader = Mockito.mock(ShardPartyPlainTextListReader.class);

        Mockito.when(reader.getShardParties()).thenReturn(Collections.singletonList(getSchedulerPartyId().getValue()));
        QuarkusMock.installMockForType(reader, ShardPartyPlainTextListReader.class, NamedLiteral.of(IAConstants.SCHEDULER_SHARD_PARTY_READER));
    }

    @Test
    void WHEN_initiation_transfers_create_on_ledger_THEN_messages_publish_to_kafka() throws InvalidProtocolBufferException, InterruptedException {
        createInitiateTransferContractOnLedger(getBank11PartyId(), getInitiateTransferRecord(GROUP_ID));
        createInitiateTransferContractOnLedger(getBank11PartyId(), getInitiateTransferRecord(GROUP_ID2));
        eventually(() -> kafkaAwaitCompletion(companion, TestWithSchedulerModeProfile.INITIATE_TRANSFER_OUTPUT_TOPIC, initiateTransfers, CONTRACT_COMPARATOR));
    }
}
