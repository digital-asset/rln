/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods.config;

import com.rln.client.damlClient.RLNClient;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.ods.TransferProposalManager;
import com.rln.gui.backend.ods.TransferProposalRepository;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

public class OdsConfig {
  @Produces
  @Singleton
  public TransferProposalManager transferProposalManager(@Named("in-memory")
                                                         TransferProposalRepository transferProposals,
                                                         GuiBackendConfiguration backendConfiguration,
                                                         RLNClient ledger) {
    var proposalManager = new TransferProposalManager(transferProposals, backendConfiguration.partyId(), ledger);
    proposalManager.run();
    return proposalManager;
  }
}
