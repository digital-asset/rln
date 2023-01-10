/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.configuration;

import com.rln.profile.TestWithBankModeProfile;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kafka.KafkaCompanionResource;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@TestProfile(TestWithBankModeProfile.class)
@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
class BankModeTest {
    public static String getConfigValue(String name) {
        return ConfigProvider.getConfig().getValue(name, String.class);
    }

    @Test
    void nonBankChannelsAreDisabled() {
        String initiation = getConfigValue("mp.messaging.incoming.initiation-message-in.enabled");
        String enriched = getConfigValue("mp.messaging.incoming.enriched-message-in.enabled");
        String approveReject = getConfigValue("mp.messaging.incoming.approve-reject-message-in.enabled");
        String finalize = getConfigValue("mp.messaging.incoming.finalize-reject-settlement-message-in.enabled");
        Assertions.assertEquals("true", initiation);
        Assertions.assertEquals("false", enriched);
        Assertions.assertEquals("true", approveReject);
        Assertions.assertEquals("false", finalize);
    }
}
