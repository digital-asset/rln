/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import lombok.Data;

@Data
public class ApproveRejectProposalChoiceParameters implements Parameters {
  private final String bankPartyId;
  private final TransferProposal.ContractId contractId;
  private final boolean approved;
  private final String reason;
  private final boolean settleOnLedger;
}
