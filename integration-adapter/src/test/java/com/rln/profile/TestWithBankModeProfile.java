/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.profile;

import com.rln.LedgerBaseTest;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;


public class TestWithBankModeProfile implements QuarkusTestProfile {
    public static final String INITIATION_INPUT_TOPIC = "initiation-in";
    public static final String APPROVE_REJECT_INPUT_TOPIC = "approve-reject-in";
    public static final String TRANSFER_PROPOSAL_OUTPUT_TOPIC = "transfer-proposal-out";
    public static final String FINALIZED_SETTLEMENT_OUTPUT_TOPIC = "finalized-settlement-out";

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("adapter.mode", "BANK",
                "daml.ledger.port", LedgerBaseTest.SANDBOX_PORT,
                "mp.messaging.outgoing.finalize-reject-settlement-message-out.topic", FINALIZED_SETTLEMENT_OUTPUT_TOPIC,
                "mp.messaging.outgoing.transfer-proposal-message-out.topic", TRANSFER_PROPOSAL_OUTPUT_TOPIC,
                "mp.messaging.incoming.initiation-message-in.topic", INITIATION_INPUT_TOPIC,
                "mp.messaging.incoming.approve-reject-message-in.topic", APPROVE_REJECT_INPUT_TOPIC);
    }
}
