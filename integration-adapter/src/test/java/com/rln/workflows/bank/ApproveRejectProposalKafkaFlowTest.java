/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.workflows.bank;

import com.daml.ledger.javaapi.data.ContractId;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.LedgerBaseTest;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.PartyCache.BankPartyCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import com.rln.profile.TestWithBankModeProfile;
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
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

@TestProfile(TestWithBankModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class ApproveRejectProposalKafkaFlowTest extends LedgerBaseTest {

    private static ContractId bank11ProposalCid;
    private static ContractId bank22ProposalCid;
    private static ContractId bank11ProposalCid1;
    protected static final TransferProposalKey TRANSFER_PROPOSAL_KEY1 = new TransferProposalKey(GROUP_ID, MESSAGE_ID, BANK11_BIC);
    protected static final TransferProposalKey TRANSFER_PROPOSAL_KEY2 = new TransferProposalKey(GROUP_ID2, MESSAGE_ID2, BANK22_BIC);
    protected static final TransferProposalKey TRANSFER_PROPOSAL_KEY3 = new TransferProposalKey(GROUP_ID2, MESSAGE_ID2, BANK11_BIC);
    protected static final ApproveRejectProposal APPROVE_REJECT_PROPOSAL1 = new ApproveRejectProposal(GROUP_ID, MESSAGE_ID, Status.APPROVE, REASON, BANK11_BIC);
    protected static final ApproveRejectProposal APPROVE_REJECT_PROPOSAL2 = new ApproveRejectProposal(GROUP_ID2, MESSAGE_ID2, Status.APPROVE, REASON, BANK22_BIC);
    protected static final ApproveRejectProposal APPROVE_REJECT_PROPOSAL3 = new ApproveRejectProposal(GROUP_ID2, MESSAGE_ID2, Status.APPROVE, REASON, BANK11_BIC);

    protected static final ApproveRejectProposal[] SINGLE_APPROVE_REJECT_PAYLOAD = new ApproveRejectProposal[] {
            APPROVE_REJECT_PROPOSAL1
    };

    protected static final ApproveRejectProposal[] MULTIPLE_APPROVE_REJECT_PAYLOAD = new ApproveRejectProposal[] {
            APPROVE_REJECT_PROPOSAL2,
            APPROVE_REJECT_PROPOSAL3
    };

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @InjectMock
    BankPartyCache bankPartyCache;

    @InjectMock
    TransferProposalCache transferProposalCache;

    @BeforeAll
    public static void publishTransferProposalsToLedger() throws InvalidProtocolBufferException {
        List<Tuple2<String, Leg>> legs1 = convertKafkaMessagesToMessageIdToLegList(MESSAGES_1, BIC_TO_PARTY_MAP);
        List<Tuple2<String, Leg>> legs2 = convertKafkaMessagesToMessageIdToLegList(MESSAGES_2, BIC_TO_PARTY_MAP);

        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID, legs1);
        populateLedgerWithTransferProposalsAndManifest(getBank11PartyId(), GROUP_ID2, legs2);

        bank11ProposalCid = SANDBOX.getLedgerAdapter().getCreatedContractId(
                getBank11PartyId(),
                TransferProposal.TEMPLATE_ID,
                ContractId::new);
        bank11ProposalCid1 = SANDBOX.getLedgerAdapter().getCreatedContractId(
                getBank11PartyId(),
                TransferProposal.TEMPLATE_ID,
                ContractId::new);
        bank22ProposalCid = SANDBOX.getLedgerAdapter().getCreatedContractId(
                getBank22PartyId(),
                TransferProposal.TEMPLATE_ID,
                ContractId::new);
    }

    @BeforeEach
    public void setCustomsMocks(){
        TransferProposal.ContractId bank11ProposalGenCid = new TransferProposal.ContractId(bank11ProposalCid.getValue());
        TransferProposal.ContractId bank22ProposalGenCid = new TransferProposal.ContractId(bank22ProposalCid.getValue());
        TransferProposal.ContractId bank11ProposalGenCid1 = new TransferProposal.ContractId(bank11ProposalCid1.getValue());

        Mockito.when(bankPartyCache.read(TRANSFER_PROPOSAL_KEY1)).thenReturn(getBank11PartyId().getValue());
        Mockito.when(bankPartyCache.read(TRANSFER_PROPOSAL_KEY2)).thenReturn(getBank22PartyId().getValue());
        Mockito.when(bankPartyCache.read(TRANSFER_PROPOSAL_KEY3)).thenReturn(getBank11PartyId().getValue());
        Mockito.when(transferProposalCache.readFromKeyToValue(TRANSFER_PROPOSAL_KEY1)).thenReturn(bank11ProposalGenCid);
        Mockito.when(transferProposalCache.readFromKeyToValue(TRANSFER_PROPOSAL_KEY2)).thenReturn(bank22ProposalGenCid);
        Mockito.when(transferProposalCache.readFromKeyToValue(TRANSFER_PROPOSAL_KEY3)).thenReturn(bank11ProposalGenCid1);
    }

    @Test
    void WHEN_approve_reject_message_publish_to_kafka_THEN_transfer_proposal_archived() throws IOException {
        produceMessageOnKafka(companion, TestWithBankModeProfile.APPROVE_REJECT_INPUT_TOPIC, SINGLE_APPROVE_REJECT_PAYLOAD);
        testContractArchived(TransferProposal.TEMPLATE_ID, getBank11PartyId(), bank11ProposalCid);
    }

    @Test
    void WHEN_multiple_reject_message_publish_to_kafka_THEN_multiple_transfer_proposal_archived() throws IOException, InterruptedException {
        produceMessageOnKafka(companion, TestWithBankModeProfile.APPROVE_REJECT_INPUT_TOPIC, MULTIPLE_APPROVE_REJECT_PAYLOAD);
        eventually( () -> testContractArchived(TransferProposal.TEMPLATE_ID, getBank11PartyId(), bank11ProposalCid1));
        eventually( () -> testContractArchived(TransferProposal.TEMPLATE_ID, getBank22PartyId(), bank22ProposalCid));
    }

}
