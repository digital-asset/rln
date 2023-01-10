/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Template;
import com.rln.damlCodegen.workflow.data.Instrument;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class TransferProposalRepositoryTest {
  private final TransferProposalRepository transferProposals = new InMemoryTransferProposalRepository();

  private static Stream<TransferProposal> proposals() {
    return Stream.of(
      createTransferProposal(),
      createApprovedTransferProposal(),
      createRejectedTransferProposal()
    );
  }

  @ParameterizedTest
  @MethodSource("proposals")
  void save_proposals(TransferProposal proposal) {
    Assertions.assertDoesNotThrow(() -> transferProposals.save(proposal));
  }

  @Test
  void find_proposals() {
    transferProposals.save(createRejectedTransferProposal());
    transferProposals.save(createRejectedTransferProposal());
    transferProposals.save(createRejectedTransferProposal());
    var proposal1 = createTransferProposal();
    transferProposals.save(proposal1);
    var proposal2 = createRejectedTransferProposal();
    transferProposals.save(proposal2);
    var proposal3 = createApprovedTransferProposal();
    transferProposals.save(proposal3);
    transferProposals.save(createRejectedTransferProposal());
    transferProposals.save(createRejectedTransferProposal());
    transferProposals.save(createApprovedTransferProposal());

    var proposals = transferProposals.findAll(3L, 3L);

    Assertions.assertIterableEquals(
      List.of(proposal1, proposal2, proposal3),
      proposals
    );
  }

  @Test
  void find_waiting_proposals() {
    transferProposals.save(createApprovedTransferProposal());
    var proposal1 = createTransferProposal();
    transferProposals.save(proposal1);
    transferProposals.save(createRejectedTransferProposal());
    var proposal2 = createTransferProposal();
    transferProposals.save(proposal2);

    var proposals = transferProposals.findAllWaiting();

    Assertions.assertIterableEquals(
      List.of(proposal1, proposal2),
      proposals
    );
  }

  private static TransferProposal createRejectedTransferProposal() {
    var createdEvent = createdEvent(
      com.rln.damlCodegen.workflow.transferproposal.RejectedTransferProposal.TEMPLATE_ID,
      new com.rln.damlCodegen.workflow.transferproposal.RejectedTransferProposal(
        "Owner",
        Instant.now(),
        Instant.now(),
        new SettlementStep(
          Optional.of("Alice"),
          Optional.of("Bob"),
          new Instrument(BigDecimal.TEN, "EUR")
        ),
        List.of("Bank1", "Bank2"),
        "Payload",
        "Message ID",
        "Group ID"
      )
    );
    return TransferProposal.createFrom(createdEvent).findFirst().get();
  }

  private static TransferProposal createApprovedTransferProposal() {
    var createdEvent = createdEvent(
      com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal.TEMPLATE_ID,
      new com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal(
        "Owner",
        Instant.now(),
        Instant.now(),
        new SettlementStep(
          Optional.of("Alice"),
          Optional.of("Bob"),
          new Instrument(BigDecimal.TEN, "EUR")
        ),
        List.of("Bank1", "Bank2"),
        "Payload",
        "Message ID",
        "Group ID"
      )
    );
    return TransferProposal.createFrom(createdEvent).findFirst().get();
  }

  private static TransferProposal createTransferProposal() {
    var createdEvent = createdEvent(
      com.rln.damlCodegen.workflow.transferproposal.TransferProposal.TEMPLATE_ID,
      new com.rln.damlCodegen.workflow.transferproposal.TransferProposal(
        "Owner",
        "Scheduler",
        "Assembler",
        Instant.now(),
        new SettlementStep(
          Optional.of("Alice"),
          Optional.of("Bob"),
          new Instrument(BigDecimal.TEN, "EUR")
        ),
        List.of("Bank1", "Bank2"),
        "Payload",
        "Message ID",
        "Group ID"
      )
    );
    return TransferProposal.createFrom(createdEvent).findFirst().get();
  }

  private static <Contract extends Template> CreatedEvent createdEvent(Identifier templateId,
                                                                       Contract contract) {
    return new CreatedEvent(
      List.of(),
      "EventId",
      templateId,
      createContractId(),
      contract.toValue(),
      Map.of(),
      Map.of(),
      Optional.empty(),
      Optional.empty(),
      List.of(),
      List.of()
    );
  }

  private static String createContractId() {
    return UUID.randomUUID().toString();
  }
}
