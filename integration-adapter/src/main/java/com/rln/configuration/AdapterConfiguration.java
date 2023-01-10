/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.configuration;

import io.smallrye.config.ConfigMapping;

import java.nio.file.Path;

@ConfigMapping(prefix = "adapter")
public interface AdapterConfiguration {
  AdapterMode mode();
  Path assemblerShardPartiesConfig();
  Path schedulerShardPartiesConfig();
  Path bankShardPartiesConfig();
  int numberOfLedgerSubscriberThreads();
  int numberOfLedgerSubmitterThreads();
  int ledgerBatchSubmissionMaxMsec();
  int ledgerBatchSubmissionMaxSize();
}
