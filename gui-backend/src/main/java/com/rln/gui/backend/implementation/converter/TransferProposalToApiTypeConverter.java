/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.converter;

import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.gui.backend.model.Transaction;

public class TransferProposalToApiTypeConverter {
  private final PartyManager partyManager;

  public TransferProposalToApiTypeConverter(PartyManager partyManager) {
    this.partyManager = partyManager;
  }

  // Looks like the endpoint is gone?
//  public static AutoapproveListInner createdEventToAutoapproveListInner(CreatedEvent createdEvent) {
//    var marker = AutoApproveTransferProposalMarker.Contract.fromCreatedEvent(createdEvent).data;
//    return AutoapproveListInner.builder()
//      .assetCode(marker.label)
//      .maxAutoApprove(marker.maxAmount)
//      .username(marker.owner)
//      .build();
//  }

  public Transaction transferProposalToTransaction(com.rln.gui.backend.ods.TransferProposal proposal) {
    return Transaction.builder()
      .id(proposal.getId())
      .transactionId(proposal.getTransactionId())
      .groupId(proposal.getGroupId())
      .messageId(proposal.getMessageId())
      .address(proposal.getAddress())
      .partyCode(partyManager.getBic(proposal.getPartyCode()))
      .assetCode(proposal.getAssetCode())
      .balance(proposal.getAmount())
      .status(proposal.getStatus())
      .build();
  }
}
