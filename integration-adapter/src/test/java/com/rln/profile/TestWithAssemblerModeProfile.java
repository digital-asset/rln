/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.profile;

import com.rln.LedgerBaseTest;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class TestWithAssemblerModeProfile implements QuarkusTestProfile {

    public static final String TRANSACTION_MANIFEST_OUTPUT_TOPIC = "transaction-manifest-out";
    public static final String APPROVE_REJECT_PROPOSAL_OUTPUT_TOPIC = "approve-reject-proposal-out";
    public static final String FINALIZE_REJECT_INPUT_TOPIC = "finalize-in";

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "daml.ledger.port", LedgerBaseTest.SANDBOX_PORT,
                "adapter.mode", "ASSEMBLER",
                "mp.messaging.outgoing.transaction-manifest-message-out.topic", TRANSACTION_MANIFEST_OUTPUT_TOPIC,
                "mp.messaging.outgoing.approve-reject-proposal-message-out.topic", APPROVE_REJECT_PROPOSAL_OUTPUT_TOPIC,
                "mp.messaging.incoming.finalize-reject-settlement-message-in.topic", FINALIZE_REJECT_INPUT_TOPIC);
    }
}
