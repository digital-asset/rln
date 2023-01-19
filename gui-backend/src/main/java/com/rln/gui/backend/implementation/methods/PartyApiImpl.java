/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.model.PartyDTO;

public class PartyApiImpl {

  private final GuiBackendConfiguration guiBackendConfiguration;
  private final PartyManager partyManager;

  public PartyApiImpl(GuiBackendConfiguration guiBackendConfiguration, PartyManager partyManager) {
    this.guiBackendConfiguration = guiBackendConfiguration;
    this.partyManager = partyManager;
  }

  public PartyDTO getMyParty() {
    var bic = partyManager.getBic(guiBackendConfiguration.partyDamlId());
    return new PartyDTO(
        guiBackendConfiguration.baseUrl(),
        bic,
        guiBackendConfiguration.partyId(),
        guiBackendConfiguration.partyName());
  }
}
