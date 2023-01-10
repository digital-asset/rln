/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.ContractId;
import lombok.Data;

import java.util.List;

@Data
public class CreateProposalsChoiceParameters implements Parameters {
  private final String schedulerPartyId;
  private final String assemblerPartyId;
  private final List<Tuple2<String, Leg>> messageIdToLegs;
  private final ContractId initiateTransferCid;
}
