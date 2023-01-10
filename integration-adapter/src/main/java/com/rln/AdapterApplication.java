/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln;

import com.rln.client.damlClient.listeners.creation.InitiationDamlListener;
import com.rln.client.damlClient.listeners.creation.ProposalDamlListener;
import com.rln.client.damlClient.listeners.creation.TransactionManifestDamlListener;
import com.rln.client.damlClient.listeners.exercise.ApproveRejectProposalDamlListener;
import com.rln.client.damlClient.listeners.exercise.FinalizeRejectSettlementDamlListener;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.configuration.AdapterConfiguration;
import com.rln.configuration.DamlLedgerConfiguration;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@QuarkusMain
public class AdapterApplication {

    public static void main(String... args) {
        System.out.println("Adapter application started..");
        Quarkus.run(AdapterApplicationStarter.class, args);
    }

    @ApplicationScoped
    public static class AdapterApplicationStarter implements QuarkusApplication {

        @Inject
        AdapterConfiguration adapterConfiguration;

        @Inject
        DamlLedgerConfiguration damlLedgerConfiguration;

        /*
         * Below are injections of self-contained bean running on its own thread
         */

        @Inject
        InitiationDamlListener initiationDamlListener;

        @Inject
        TransactionManifestDamlListener transactionManifestDamlListener;

        @Inject
        ApproveRejectProposalDamlListener acceptRejectDamlListener;

        @Inject
        FinalizeRejectSettlementDamlListener finalizeRejectSettlementDamlListener;

        @Inject
        ProposalDamlListener proposalDamlListener;

        @Inject
        PartyManager bankBicPartyManager;

        @Override
        public int run(String... args) {
            System.out.println("Mode: " + adapterConfiguration.mode().name());
            System.out.println("Daml ledger host/port: " + damlLedgerConfiguration.host() + " " + damlLedgerConfiguration.port());
            Quarkus.waitForExit();
            return 0;
        }
    }
}
