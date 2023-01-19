/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.producer;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.damlClient.partyManagement.BicPartyIdMapper;
import com.rln.client.damlClient.partyManagement.DamlBankBicPartyManager;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class PartyManagementProducer {
  Logger logger = LoggerFactory.getLogger(PartyManagementProducer.class);

  @Singleton
  @Produces
  public BicPartyIdMapper getBicPartyIdMapper(RLNClient rlnClient, GuiBackendConfiguration guiBackendConfiguration) {
    logger.info("Created BicPartyIdMapper with RLNClient {}", rlnClient);
    return new BicPartyIdMapper(rlnClient, guiBackendConfiguration.partyDamlId());
  }

  @ApplicationScoped
  @Produces
  public PartyManager getDamlBankBicPartyManager(BicPartyIdMapper mapper) {
    logger.info("Created DamlBankBicPartyManager with mapper {}", mapper);
    return new DamlBankBicPartyManager(mapper);
  }
}
