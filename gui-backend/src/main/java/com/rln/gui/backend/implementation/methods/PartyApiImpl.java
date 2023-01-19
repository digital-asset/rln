/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.implementation.config.SetlParty;
import com.rln.gui.backend.model.PartyDTO;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PartyApiImpl {

  private Supplier<List<SetlParty>> setlPartySupplier;

  private final GuiBackendConfiguration guiBackendConfiguration;
  private final PartyManager partyManager;
  public PartyApiImpl(GuiBackendConfiguration guiBackendConfiguration, PartyManager partyManager) {
    this.guiBackendConfiguration = guiBackendConfiguration;
    this.partyManager = partyManager;
    this.setlPartySupplier = this::readSetlParties;
  }

  public PartyDTO getMyParty() {
    var bic = partyManager.getBic(guiBackendConfiguration.partyDamlId());
    return new PartyDTO(
        guiBackendConfiguration.baseUrl(),
        bic,
        guiBackendConfiguration.partyId(),
        guiBackendConfiguration.partyName());
  }

  public List<PartyDTO> getParties() {
    return setlPartySupplier.get().stream()
        .map(this::toPartyDTO)
        .collect(Collectors.toList());
  }

  void setSetlPartySupplier(
      Supplier<List<SetlParty>> setlPartySupplier) {
    this.setlPartySupplier = setlPartySupplier;
  }

  private PartyDTO toPartyDTO(SetlParty setlParty) {
    return new PartyDTO(
        setlParty.getBaseUrl(),
        partyManager.getBic(setlParty.getDamlPartyId()),
        setlParty.getId(),
        setlParty.getName());
  }

  List<SetlParty> readSetlParties() {
    try {
      return new ObjectMapper().readValue(
          guiBackendConfiguration.partiesConfig().toFile(),
          new TypeReference<>() {}
      );
    } catch (IOException e) {
      throw new SetlPartiesConfigFileNotFoundException(guiBackendConfiguration.partiesConfig());
    }
  }
}
