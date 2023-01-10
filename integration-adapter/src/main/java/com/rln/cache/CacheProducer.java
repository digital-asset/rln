/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache;

import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.PartyCache.AssemblerPartyCache;
import com.rln.cache.PartyCache.BankPartyCache;
import com.rln.cache.PartyCache.SchedulerPartyCache;
import com.rln.configuration.AdapterConfiguration;
import com.rln.configuration.AdapterMode;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.List;

public class CacheProducer {

    @ApplicationScoped
    @Produces
    InitiateTransferCache getInitiateTransferCache(AdapterConfiguration adapterConfiguration) {
        if (adapterConfiguration.mode().equals(AdapterMode.SCHEDULER)) {
            return new InitiateTransferCache();
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    TransferProposalCache getTransferProposalCache(AdapterConfiguration adapterConfiguration) {
        if (List.of(AdapterMode.BANK, AdapterMode.ASSEMBLER).contains(adapterConfiguration.mode())) {
            return new TransferProposalCache();
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    TransactionManifestCache getTransactionManifestCache(AdapterConfiguration adapterConfiguration) {
        if (List.of(AdapterMode.BANK, AdapterMode.ASSEMBLER).contains(adapterConfiguration.mode())) {
            return new TransactionManifestCache();
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    SchedulerPartyCache getSchedulerPartyCache(AdapterConfiguration adapterConfiguration) {
        if (adapterConfiguration.mode().equals(AdapterMode.SCHEDULER)) {
            return new SchedulerPartyCache();
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    BankPartyCache getBankPartyCache(AdapterConfiguration adapterConfiguration) {
        if (adapterConfiguration.mode().equals(AdapterMode.BANK)) {
            return new BankPartyCache();
        }
        return null;
    }

    @ApplicationScoped
    @Produces
    AssemblerPartyCache getAssemblerPartyCache(AdapterConfiguration adapterConfiguration) {
        if (adapterConfiguration.mode().equals(AdapterMode.ASSEMBLER)) {
            return new AssemblerPartyCache();
        }
        return null;
    }
}
