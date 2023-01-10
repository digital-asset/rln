/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.configuration;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "daml.ledger")
public interface DamlLedgerConfiguration {
  String host();
  int port();
  String bankBicReadingPartyId();
}
