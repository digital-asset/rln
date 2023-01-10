/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln;

import com.rln.client.damlClient.listeners.creation.InitiationDamlListener;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.inject.Inject;

@QuarkusTest
class AdapterApplicationTest {

    @Inject
    AdapterApplication application;

    @Inject
    InitiationDamlListener initiationDamlListener;

    @BeforeAll
    static void setup() {
        RLNLedgerSubscriber mock = Mockito.mock(RLNLedgerSubscriber.class);
        Mockito.when(mock.subscribe(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);
        QuarkusMock.installMockForType(mock, RLNLedgerSubscriber.class);
    }

    @Test
    void startup() {}
}
