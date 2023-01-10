/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.profile;

import com.rln.LedgerBaseTest;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class TestWithSchedulerModeProfile implements QuarkusTestProfile {
    public static final String INITIATE_TRANSFER_OUTPUT_TOPIC = "initiation-out";
    public static final String ENRICHED_PAC008_INPUT_TOPIC = "enriched-in";

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "daml.ledger.port", LedgerBaseTest.SANDBOX_PORT,
                "adapter.mode", "SCHEDULER",
                "mp.messaging.incoming.enriched-message-in.topic", ENRICHED_PAC008_INPUT_TOPIC,
                "mp.messaging.outgoing.initiation-message-out.topic", INITIATE_TRANSFER_OUTPUT_TOPIC);
    }
}
