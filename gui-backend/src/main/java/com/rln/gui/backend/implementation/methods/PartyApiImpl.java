/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.implementation.config.SetlParty;
import com.rln.gui.backend.model.ClientDTO;
import com.rln.gui.backend.model.PartyDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartyApiImpl {

  private final SetlPartySupplier setlPartySupplier;
  private final GuiBackendConfiguration guiBackendConfiguration;
  private final PartyManager partyManager;

  public PartyApiImpl(GuiBackendConfiguration guiBackendConfiguration, PartyManager partyManager,
      SetlPartySupplier setlPartySupplier) {
    this.guiBackendConfiguration = guiBackendConfiguration;
    this.partyManager = partyManager;
    this.setlPartySupplier = setlPartySupplier;
  }

  public PartyDTO getMyParty() {
    var bic = partyManager.getBic(guiBackendConfiguration.partyDamlId());
    return new PartyDTO(
        guiBackendConfiguration.baseUrl(),
        List.of(bic),
        guiBackendConfiguration.partyId(),
        guiBackendConfiguration.partyName());
  }

  public List<PartyDTO> getParties() {
    return setlPartySupplier.getParties().stream()
        .map(this::toPartyDTO)
        .collect(Collectors.toList());
  }

  private PartyDTO toPartyDTO(SetlParty setlParty) {
    return new PartyDTO(
        setlParty.getBaseUrl(),
        List.of(partyManager.getBic(setlParty.getDamlPartyId())),
        setlParty.getId(),
        setlParty.getName());
  }

  public List<ClientDTO> getClients() {
    var clients = setlPartySupplier
        .getSetlPartyByDamlParty(guiBackendConfiguration.partyDamlId())
        .getClients().stream()
        .map(setlClient ->
            ClientDTO.builder()
             .id(setlClient.getClientId())
             .name(setlClient.getName())
            .build());

    var treasuries = setlPartySupplier
            .getTreasuryAccountsByProviderParty(guiBackendConfiguration.partyDamlId())
            .stream().map(treasuryAccount ->
                    ClientDTO.builder()
                            .id(treasuryAccount.getClientId())
                            .name(treasuryAccount.getIban())
                            .build());

    return Stream.concat(clients, treasuries).collect(Collectors.toList());
  }
}
