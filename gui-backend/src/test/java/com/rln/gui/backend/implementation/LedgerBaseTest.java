/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation;

import com.daml.extensions.testing.Dsl;
import com.daml.extensions.testing.ledger.SandboxManager;
import com.daml.ledger.javaapi.data.ContractId;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.daml.ledger.javaapi.data.codegen.Exercised;
import com.daml.ledger.javaapi.data.codegen.Update;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.client.damlClient.TestUtils;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Instrument;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class LedgerBaseTest {
  public static final String SANDBOX_PORT = "6863";
  public static final String PAYLOAD = "PAYLOAD";

  protected static final Path DAR_PATH =
      Path.of("../stagingtest/canton/rln.dar").toAbsolutePath();
  protected static final Path DAML_ROOT = Path.of("../daml-model").toAbsolutePath();
  protected static final String TEST_BANK_BICS_MODULE = "Tests.BankBICsTest";
  protected static final String POPULATE_BANKS_SCRIPT = "populateBanks";

  protected static final String GID_FIELD_NAME = "groupId";
  protected static final String INITIATOR_FIELD_NAME = "initiator";
  protected static final String SCHEDULER_FIELD_NAME = "scheduler";
  protected static final String PAYLOAD_FIELD_NAME = "payload";

  protected static final String DISPLAY_NAME_SCHEDULER = "Scheduler";
  protected static final String DISPLAY_NAME_ASSEMBLER = "Assembler";
  protected static final String DISPLAY_NAME_BANK11_BANK = "Bank11";

  protected static final String GROUP_ID = "GROUP_ID";

  protected static final String BANK_BIC = "BANK11_BIC";
  protected static final String SENDER_IBAN = "SENDER_IBAN";
  protected static final String RECEIVER_IBAN = "RECEIVER_IBAN";
  protected static final String SENDER_BIC = "CHASUS33XXX";
  protected static final String RECEIVER_BIC = "CSCHUS6SXXX";
  protected static final String USD = "USD";
  protected static final String MESSAGE_ID = "MsgId1";
  protected static final String TRANSACTION_ID = "MsgId1";
  // Scaling is used because Daml uses 10 digits after `.` and if we do not set this,
  // our contract matchers won't work.
  protected static final BigDecimal TRANSACTION_AMOUNT = BigDecimal.TEN.setScale(10);
  protected static final BigDecimal TRANSACTION_AMOUNT_2 = BigDecimal.ONE.setScale(10);
  protected static final Instrument USD_INSTRUMENT = new Instrument(TRANSACTION_AMOUNT, USD);
  protected static final SettlementStep USD_INSTRUMENT_SETTLEMENT_STEP =
      new SettlementStep(Optional.of(SENDER_IBAN), Optional.of(RECEIVER_IBAN), USD_INSTRUMENT);
  protected static final PartyManager mockPartyManager = Mockito.mock(PartyManager.class);
  protected static final SandboxManager SANDBOX =
      new SandboxManager(
          DAML_ROOT,
          Optional.of(TEST_BANK_BICS_MODULE),
          Optional.of(POPULATE_BANKS_SCRIPT),
          Optional.of(Integer.parseInt(SANDBOX_PORT)),
          Duration.ofSeconds(30),
          Duration.ofSeconds(10),
          new String[] {},
          DAR_PATH,
          (client, channel) -> {},
          false);
  protected static final String BASEURL = "baseurl";
  protected static final String PARTY_NAME = "PartyName";
  protected static final long PARTY_ID = 1L;
  private static Party currentBankPartyId;
  private static Party schedulerPartyId;
  private static Party assemblerPartyId;

  public static void cleanupContract(Party partyId, Identifier identifier, String contractId)
      throws InvalidProtocolBufferException {
    SANDBOX.getLedgerAdapter().exerciseChoice(partyId,
        new ExerciseCommand(identifier, contractId, "Archive", new DamlRecord()));
  }

  @ApplicationScoped
  @Mock
  public static class MockedGuiBackendConfiguration implements GuiBackendConfiguration {
    @Override
    public Long partyId() {
      return PARTY_ID;
    }

    @Override
    public String partyDamlId() {
      return currentBankPartyId.getValue();
    }

    @Override
    public String partyName() {
      return PARTY_NAME;
    }

    @Override
    public String baseUrl() {
      return BASEURL;
    }

    @Override
    public Path schedulerShardPartiesConfig() {
      return null;
    }

    @Override
    public int numberOfLedgerSubmitterThreads() {
      return 1;
    }

    @Override
    public int ledgerBatchSubmissionMaxMsec() {
      return 1000;
    }

    @Override
    public int ledgerBatchSubmissionMaxSize() {
      return 1;
    }
  }

  @BeforeAll
  public static void setup() {
    Mockito.when(mockPartyManager.getBic(ArgumentMatchers.eq(currentBankPartyId.getValue()))).thenReturn(BANK_BIC);
    QuarkusMock.installMockForType(mockPartyManager, PartyManager.class);
  }

  public static ContractId publishTransferProposalToLedger(
      Party bank,
      Party scheduler,
      Party assembler,
      String groupId,
      String messageId,
      SettlementStep settlementStep)
      throws InvalidProtocolBufferException {
    Tuple2<String, SettlementStep> singleStep = new Tuple2<>(bank.getValue(), settlementStep);
    List<Tuple2<String, SettlementStep>> approversToSettlementSteps = List.of(singleStep);
    Leg leg = new Leg(PAYLOAD, approversToSettlementSteps);
    List<Tuple2<String, Leg>> legs = List.of(new Tuple2<>(messageId, leg));
    populateLedgerWithTransferProposalsAndManifest(scheduler, assembler, bank, groupId, legs);

    return SANDBOX
        .getLedgerAdapter()
        .getCreatedContractId(bank, TransferProposal.TEMPLATE_ID, ContractId::new);
  }

  @BeforeAll
  protected static void beforeAll() throws IOException, InterruptedException, TimeoutException {
    SANDBOX.start();
    setUpPartyVariables();
  }

  @AfterAll
  protected static void stopSandbox() {
    SANDBOX.stop();
  }

  protected static void setUpPartyVariables() {
    currentBankPartyId = SANDBOX.getPartyId(DISPLAY_NAME_BANK11_BANK);
    schedulerPartyId = SANDBOX.getPartyId(DISPLAY_NAME_SCHEDULER);
    assemblerPartyId = SANDBOX.getPartyId(DISPLAY_NAME_ASSEMBLER);
  }

  protected static DamlRecord getInitiateTransferRecord(
      String groupId, Party initiator, Party scheduler) {
    return Dsl.record(
        Dsl.field(GID_FIELD_NAME, Dsl.text(groupId)),
        Dsl.field(INITIATOR_FIELD_NAME, initiator),
        Dsl.field(SCHEDULER_FIELD_NAME, scheduler),
        Dsl.field(PAYLOAD_FIELD_NAME, Dsl.text(PAYLOAD)));
  }

  protected static InitiateTransfer.ContractId createInitiateTransferContractOnLedger(
      Party initiatorParty, DamlRecord record) throws InvalidProtocolBufferException {
    SANDBOX.getLedgerAdapter().createContract(initiatorParty, InitiateTransfer.TEMPLATE_ID, record);
    return SANDBOX
        .getLedgerAdapter()
        .getCreatedContractId(
            initiatorParty,
            com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.TEMPLATE_ID,
            InitiateTransfer.ContractId::new);
  }

  protected static ExerciseCommand prepareLedgerAndGetCreateProposalCommand(
      Party scheduler,
      Party assembler,
      Party initiator,
      String groupId,
      List<Tuple2<String, Leg>> legs)
      throws InvalidProtocolBufferException {
    var initiateTransferCid =
        createInitiateTransferContractOnLedger(
            initiator, getInitiateTransferRecord(groupId, initiator, scheduler));
    return TestUtils.toExerciseCommand(initiateTransferCid.exerciseCreateProposals(legs, assembler.getValue()));
  }

  protected static void populateLedgerWithTransferProposalsAndManifest(
      Party scheduler,
      Party assembler,
      Party initiator,
      String groupId,
      List<Tuple2<String, Leg>> legs)
      throws InvalidProtocolBufferException {
    var exerciseCreateProposals =
        prepareLedgerAndGetCreateProposalCommand(scheduler, assembler, initiator, groupId, legs);
    SANDBOX.getLedgerAdapter().exerciseChoice(scheduler, exerciseCreateProposals);
  }

  public static Party getCurrentBankPartyId() {
    return currentBankPartyId;
  }

  public static Party getSchedulerPartyId() {
    return schedulerPartyId;
  }

  public static Party getAssemblerPartyId() {
    return assemblerPartyId;
  }
}
