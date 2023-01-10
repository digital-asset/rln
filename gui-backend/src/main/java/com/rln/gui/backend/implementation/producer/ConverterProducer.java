/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.producer;

import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.gui.backend.implementation.converter.TransferProposalToApiTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class ConverterProducer {
  Logger logger = LoggerFactory.getLogger(ConverterProducer.class);

  @ApplicationScoped
  @Produces
  public TransferProposalToApiTypeConverter getTransferProposalToApiTypeConverter(PartyManager partyManager) {
    logger.info("Created TransferProposalToApiTypeConverter");
    return new TransferProposalToApiTypeConverter(partyManager);
  }
}
