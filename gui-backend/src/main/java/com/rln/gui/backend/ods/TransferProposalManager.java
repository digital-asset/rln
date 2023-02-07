/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods;

import com.daml.ledger.javaapi.data.ArchivedEvent;
import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.FiltersByParty;
import com.daml.ledger.javaapi.data.GetActiveContractsResponse;
import com.daml.ledger.javaapi.data.InclusiveFilter;
import com.daml.ledger.javaapi.data.LedgerOffset;
import com.rln.client.damlClient.RLNClient;
import com.rln.damlCodegen.workflow.transactionmanifest.SettlementFinalized;
import com.rln.damlCodegen.workflow.transactionmanifest.SettlementRejected;
import com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.RejectedTransferProposal;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransferProposalManager {

  private static final InclusiveFilter CONTRACT_FILTER = new InclusiveFilter(
      Set.of(
          TransferProposal.TEMPLATE_ID,
          ApprovedTransferProposal.TEMPLATE_ID,
          RejectedTransferProposal.TEMPLATE_ID,
          SettlementFinalized.TEMPLATE_ID,
          SettlementRejected.TEMPLATE_ID
      ),
      Map.of()
  );
  private final FiltersByParty proposalFilter;
  private final TransferProposalRepository transferProposals;
  private final RLNClient ledger;

  public TransferProposalManager(TransferProposalRepository transferProposals,
      String partyId,
      RLNClient ledger) {
    this.transferProposals = transferProposals;
    this.proposalFilter = new FiltersByParty(Map.of(partyId, CONTRACT_FILTER));
    this.ledger = ledger;
  }

  public void run() {
    ledger
        .getActiveContractsResponse(proposalFilter)
        .reduce((LedgerOffset) LedgerOffset.LedgerBegin.getInstance(), (offset, acs) -> {
          acs
              .getCreatedEvents()
              .forEach(this::handleCreateEvent);
          return getLedgerOffset(acs, offset);
        })
        .toFlowable()
        .concatMap(offset -> ledger.getTransactions(offset, proposalFilter))
        .subscribe(transaction -> {
          var archives = new HashSet<String>();
          for (var event : transaction.getEvents()) {
            if (event instanceof ArchivedEvent) {
              archives.add(event.getContractId());
            } else {
              handleCreateEvent((CreatedEvent) event);
            }
          }
          transferProposals.deleteAllByContractId(archives);
        });
  }

  private void handleCreateEvent(CreatedEvent event) {
    if (event.getTemplateId().equals(SettlementFinalized.TEMPLATE_ID)) {
      var settlementFinalized = SettlementFinalized.valueDecoder().decode(event.getArguments());
      transferProposals.update(hasGroupId(settlementFinalized.groupId),
          com.rln.gui.backend.ods.TransferProposal::finalizeSettlement);
    } else if (event.getTemplateId().equals(SettlementRejected.TEMPLATE_ID)) {
      var settlementRejected = SettlementRejected.valueDecoder().decode(event.getArguments());
      transferProposals.update(hasGroupId(settlementRejected.groupId),
          com.rln.gui.backend.ods.TransferProposal::rejectSettlement);
    } else {
      com.rln.gui.backend.ods.TransferProposal
          .createFrom(event)
          .forEach(transferProposals::save);
    }
  }

  private Predicate<com.rln.gui.backend.ods.TransferProposal> hasGroupId(String groupId) {
    return transferProposal -> Objects.equals(transferProposal.getGroupId(), groupId);
  }

  private static LedgerOffset getLedgerOffset(GetActiveContractsResponse acs,
      LedgerOffset defaultOffset) {
    Function<String, LedgerOffset> absoluteOffset = LedgerOffset.Absolute::new;
    return acs.getOffset().map(absoluteOffset).orElse(defaultOffset);
  }
}
