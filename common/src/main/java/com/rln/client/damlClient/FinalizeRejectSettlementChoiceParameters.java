/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.ContractId;
import lombok.Data;

@Data
public class FinalizeRejectSettlementChoiceParameters implements Parameters {
  private final String assemblerPartyId;
  private final boolean approved;
  private final String reason;
  private final ContractId transactionManifestCid;
}
