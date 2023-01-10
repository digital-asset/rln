/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.config;

import io.smallrye.config.ConfigMapping;

import java.nio.file.Path;

@ConfigMapping(prefix = "rln.gui.backend")
public interface GuiBackendConfiguration {
  String partyId();
  Path schedulerShardPartiesConfig();
  int numberOfLedgerSubmitterThreads();
  int ledgerBatchSubmissionMaxMsec();
  int ledgerBatchSubmissionMaxSize();
}
