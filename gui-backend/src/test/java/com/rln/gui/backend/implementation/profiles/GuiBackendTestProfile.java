/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.profiles;

import com.rln.gui.backend.implementation.methods.LedgerBaseTest;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class GuiBackendTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.of(
        "daml.ledger.port", LedgerBaseTest.SANDBOX_PORT);
  }
}
