/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln;

import com.daml.extensions.testing.Dsl;
import com.daml.extensions.testing.comparator.ledger.ContractArchived;
import com.daml.extensions.testing.ledger.SandboxManager;
import com.daml.ledger.javaapi.data.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.client.damlClient.TestUtils;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.kafkaClient.message.fields.MessageIdWithStepsAndPayload;
import com.rln.client.kafkaClient.message.fields.Step;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import io.quarkus.test.junit.QuarkusMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class LedgerBaseTest extends CommonBaseTest {

    public static final String SANDBOX_PORT = "6863";

    protected static final Path DAR_PATH = Path.of("../daml-model/build/daml/rln.dar").toAbsolutePath();
    protected static final Path DAML_ROOT = Path.of("../daml-model").toAbsolutePath();
    protected static final String TEST_BANK_BICS_MODULE = "Tests.BankBICsTest";
    protected static final String POPULATE_BANKS_SCRIPT = "populateBanks";
    // Display names aligned with DAML scripts

    protected static final String DISPLAY_NAME_SCHEDULER = "Scheduler";
    protected static final String DISPLAY_NAME_ASSEMBLER = "Assembler";
    protected static final String DISPLAY_NAME_BANK11_BANK = "Bank11";
    protected static final String DISPLAY_NAME_BANK22_BANK = "Bank22";
    protected static final String DISPLAY_NAME_BANK12_BANK = "Bank12";
    protected static final String DISPLAY_NAME_BANK21_BANK = "Bank21";
    protected static final String DISPLAY_NAME_CB_BANK = "CentralBank";

    protected static final String GID_FIELD_NAME = "groupId";
    protected static final String INITIATOR_FIELD_NAME = "initiator";
    protected static final String SCHEDULER_FIELD_NAME = "scheduler";
    protected static final String PAYLOAD_FIELD_NAME = "payload";

    protected static final SandboxManager SANDBOX = new SandboxManager(
            DAML_ROOT,
            Optional.of(TEST_BANK_BICS_MODULE),
            Optional.of(POPULATE_BANKS_SCRIPT),
            Optional.of(Integer.parseInt(SANDBOX_PORT)),
            Duration.ofSeconds(30),
            Duration.ofSeconds(10),
            new String[]{},
            DAR_PATH,
            (client, channel) -> {
            },
            false);

    private static Party bank11PartyId;
    private static Party schedulerPartyId;
    private static Party assemblerPartyId;
    private static Party bank22PartyId;
    private static Party bank12PartyId;
    private static Party bank21PartyId;
    private static Party bankCBPartyId;

    protected static final Map<String, Party> BIC_TO_PARTY_MAP = new HashMap<>();

    protected static final Step STEP1 = new Step(BANK11_BIC, SENDER_IBAN, RECEIVER_IBAN, 10.0, USD);
    protected static final Step STEP2 = new Step(BANK22_BIC, RECEIVER_IBAN, SENDER_IBAN, 10.0, EUR);

    protected static final MessageIdWithStepsAndPayload[] MESSAGES_1 = new MessageIdWithStepsAndPayload[]{
            new MessageIdWithStepsAndPayload(MESSAGE_ID, new Step[]{STEP1}, PAYLOAD)
    };

    protected static final MessageIdWithStepsAndPayload[] MESSAGES_2 = new MessageIdWithStepsAndPayload[]{
            new MessageIdWithStepsAndPayload(MESSAGE_ID2, new Step[]{STEP1}, PAYLOAD),
            new MessageIdWithStepsAndPayload(MESSAGE_ID3, new Step[]{STEP2}, PAYLOAD)
    };

    protected static void setUpPartyVariables(){
        bank11PartyId = SANDBOX.getPartyId(DISPLAY_NAME_BANK11_BANK);
        bank22PartyId = SANDBOX.getPartyId(DISPLAY_NAME_BANK22_BANK);
        bank12PartyId = SANDBOX.getPartyId(DISPLAY_NAME_BANK12_BANK);
        bank21PartyId = SANDBOX.getPartyId(DISPLAY_NAME_BANK21_BANK);
        bankCBPartyId = SANDBOX.getPartyId(DISPLAY_NAME_CB_BANK);
        schedulerPartyId = SANDBOX.getPartyId(DISPLAY_NAME_SCHEDULER);
        assemblerPartyId = SANDBOX.getPartyId(DISPLAY_NAME_ASSEMBLER);

        BIC_TO_PARTY_MAP.put(BANK11_BIC, bank11PartyId);
        BIC_TO_PARTY_MAP.put(BANK22_BIC, bank22PartyId);
        BIC_TO_PARTY_MAP.put(BANK12_BIC, bank12PartyId);
        BIC_TO_PARTY_MAP.put(BANK21_BIC, bank21PartyId);
        BIC_TO_PARTY_MAP.put(BANKCB_BIC, bankCBPartyId);
    }

    protected static void setCommonPartyManagerMocks(){
        Mockito.when(partyManager.getParty(BANK11_BIC)).thenReturn(bank11PartyId.getValue());
        Mockito.when(partyManager.getParty(BANK22_BIC)).thenReturn(bank22PartyId.getValue());
        Mockito.when(partyManager.getParty(BANK12_BIC)).thenReturn(bank12PartyId.getValue());
        Mockito.when(partyManager.getParty(BANK21_BIC)).thenReturn(bank21PartyId.getValue());
        Mockito.when(partyManager.getParty(BANKCB_BIC)).thenReturn(bankCBPartyId.getValue());
        Mockito.when(partyManager.getBic(bank11PartyId.getValue())).thenReturn(BANK11_BIC);
        Mockito.when(partyManager.getBic(bank22PartyId.getValue())).thenReturn(BANK22_BIC);
        Mockito.when(partyManager.getBic(bank12PartyId.getValue())).thenReturn(BANK12_BIC);
        Mockito.when(partyManager.getBic(bank21PartyId.getValue())).thenReturn(BANK21_BIC);
        Mockito.when(partyManager.getBic(bankCBPartyId.getValue())).thenReturn(BANKCB_BIC);
        Mockito.when(partyManager.hasBic(BANK11_BIC)).thenReturn(true);
        Mockito.when(partyManager.hasBic(BANK22_BIC)).thenReturn(true);
        Mockito.when(partyManager.hasBic(BANK12_BIC)).thenReturn(true);
        Mockito.when(partyManager.hasBic(BANK21_BIC)).thenReturn(true);
        Mockito.when(partyManager.hasBic(BANKCB_BIC)).thenReturn(true);
        QuarkusMock.installMockForType(partyManager, PartyManager.class);
    }

    protected static DamlRecord getInitiateTransferRecord(String groupId) {
        return Dsl.record(
                Dsl.field(GID_FIELD_NAME, Dsl.text(groupId)),
                Dsl.field(INITIATOR_FIELD_NAME, bank11PartyId),
                Dsl.field(SCHEDULER_FIELD_NAME, schedulerPartyId),
                Dsl.field(PAYLOAD_FIELD_NAME, Dsl.text(PAYLOAD)));
    }

    protected static InitiateTransferCache.InitiatorAndContractId createInitiateTransferContractOnLedger(Party initiatorParty, DamlRecord record) throws InvalidProtocolBufferException {
        SANDBOX.getLedgerAdapter().createContract(initiatorParty, InitiateTransfer.TEMPLATE_ID, record);
        InitiateTransfer.ContractId initiateTransferCid =
                SANDBOX.getLedgerAdapter().getCreatedContractId(
                        initiatorParty,
                        com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.TEMPLATE_ID,
                        InitiateTransfer.ContractId::new);

        return new InitiateTransferCache.InitiatorAndContractId(initiatorParty.getValue(),initiateTransferCid);
    }

    protected Party[] translateBicsInStepsToParties(Step[] steps){
        return Arrays.stream(steps).map(Step::getApprover).map(BIC_TO_PARTY_MAP::get).toArray(Party[]::new);
    }

    protected static MessageIdWithStepsAndPayload[] getMessageIdWithStepsAndPayload(String messageId) {
        return new MessageIdWithStepsAndPayload[] {
            new MessageIdWithStepsAndPayload(messageId, new Step[]{new Step(BANK11_BIC, SENDER_IBAN, RECEIVER_IBAN, 10.0, USD)}, PAYLOAD)
        };
    }

    protected static <Cid> void lookUpContractWithMatcher(
            Identifier contractIdentifier,
            Function<String, Cid> cidConstructor,
            List<DamlRecord> matchers,
            Party observer){
        Assertions.assertFalse(matchers.isEmpty());
        for (DamlRecord record: matchers) {
            SANDBOX.getLedgerAdapter().getCreatedContractId(
                    observer,
                    contractIdentifier,
                    record,
                    cidConstructor);
        }
    }

    protected void testContractArchived(Identifier templateId, Party observer, ContractId contractId) {
        SANDBOX.getLedgerAdapter().observeEvent(
                observer.getValue(),
                ContractArchived.apply(
                        templateId.toString(),
                        contractId));
    }

    protected static ExerciseCommand prepareLedgerAndGetCreateProposalCommand(Party initiator, String groupId, List<Tuple2<String, Leg>> legs) throws InvalidProtocolBufferException {
        InitiateTransferCache.InitiatorAndContractId initiateTransferCid = createInitiateTransferContractOnLedger(initiator, getInitiateTransferRecord(groupId));
        return TestUtils.toExerciseCommand(initiateTransferCid.contractId.exerciseCreateProposals(legs, assemblerPartyId.getValue()));
    }

    protected static void populateLedgerWithTransferProposalsAndManifest(Party initiator, String groupId, List<Tuple2<String, Leg>> legs) throws InvalidProtocolBufferException {
        var exerciseCreateProposals = prepareLedgerAndGetCreateProposalCommand(initiator, groupId, legs);
        SANDBOX.getLedgerAdapter().exerciseChoice(schedulerPartyId, exerciseCreateProposals);
    }

    @BeforeAll
    protected static void beforeAll() throws IOException, InterruptedException, TimeoutException {
        SANDBOX.start();
        refreshCommonTestSetUp();
    }

    @AfterAll
    protected static void stopSandbox(){
        SANDBOX.stop();
    }

    private static void refreshCommonTestSetUp() throws IOException, InterruptedException, TimeoutException {
        setUpPartyVariables();
        setCommonPartyManagerMocks();
    }

    public static Party getBank11PartyId() {
        return bank11PartyId;
    }

    public static Party getSchedulerPartyId() {
        return schedulerPartyId;
    }

    public static Party getAssemblerPartyId() {
        return assemblerPartyId;
    }

    public static Party getBank22PartyId() {
        return bank22PartyId;
    }

    public static Party getBank12PartyId() {
        return bank12PartyId;
    }

    public static Party getBank21PartyId() {
        return bank21PartyId;
    }

    public static Party getBankCBPartyId() {
        return bankCBPartyId;
    }
}
