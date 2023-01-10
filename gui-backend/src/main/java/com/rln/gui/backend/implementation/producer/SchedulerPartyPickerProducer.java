/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.producer;

import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.client.damlClient.partyManagement.ShardPartyPlainTextListReader;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.FileNotFoundException;

public class SchedulerPartyPickerProducer {
  @ApplicationScoped
  @Produces
  public RandomShardPartyPicker getRandomShardPartyPicker(
          ShardPartyPlainTextListReader schedulerShardPartyPlainTextListReader) {
    return new RandomShardPartyPicker(schedulerShardPartyPlainTextListReader);
  }

  @ApplicationScoped
  @Produces
  public ShardPartyPlainTextListReader getSchedulerShardPartyReader(
      GuiBackendConfiguration guiBackendConfiguration) throws FileNotFoundException {
    return ShardPartyPlainTextListReader.initializeFromPath(guiBackendConfiguration.schedulerShardPartiesConfig());
  }
}
