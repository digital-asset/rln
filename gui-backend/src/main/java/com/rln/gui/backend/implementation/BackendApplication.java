/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation;

import com.rln.client.damlClient.partyManagement.BicPartyIdMapper;
import com.rln.gui.backend.implementation.balanceManagement.AccountEventListener;
import com.rln.gui.backend.implementation.balanceManagement.AutoApproveEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceEventListener;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.ods.TransferProposalManager;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@QuarkusMain
public class BackendApplication {

    public static void main(String... args) {
        System.out.println("Backend application started..");
        Quarkus.run(BackendApplicationStarter.class, args);
    }

    @ApplicationScoped
    public static class BackendApplicationStarter implements QuarkusApplication {
        @Inject
        TransferProposalManager transferProposalManager;

        @Inject
        BalanceEventListener balanceEventListener;

        @Inject
        AccountEventListener accountEventListener;

        @Inject
        AutoApproveEventListener autoApproveEventListener;

        @Inject
        BicPartyIdMapper bicPartyIdMapper;

        @Inject
        GuiBackendConfiguration configuration;

        @Override
        public int run(String... args) {
            System.out.println("Backend application running.");
            Quarkus.waitForExit();
            return 0;
        }
    }
}
