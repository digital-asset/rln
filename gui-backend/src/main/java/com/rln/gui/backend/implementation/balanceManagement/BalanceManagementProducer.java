/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.rln.client.damlClient.RLNClient;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.AutoApproveCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.IncomingBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LockedBalanceCache;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import javax.enterprise.context.Dependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class BalanceManagementProducer {
    Logger logger = LoggerFactory.getLogger(BalanceEventProcessor.class);

    @Dependent
    @Produces
    public BalanceEventListener getBalanceEventListener(RLNClient rlnClient,
                                                        BalanceEventProcessor processor,
                                                        GuiBackendConfiguration guiConfig) {
        logger.info("Created BalanceEventListener");
        return new BalanceEventListener(rlnClient, processor, guiConfig.partyDamlId());
    }

    @ApplicationScoped
    @Produces
    public BalanceEventProcessor getBalanceEventProcessor(LiquidBalanceCache liquidBalanceCache,
                                                          LockedBalanceCache lockedBalanceCache,
                                                          IncomingBalanceCache incomingBalanceCache
                                                          ) {
        logger.info("Created BalanceEventProcessor");
        return new BalanceEventProcessor(liquidBalanceCache, lockedBalanceCache, incomingBalanceCache);
    }

    @Dependent
    @Produces
    public AccountEventListener getAccountEventListener(RLNClient rlnClient,
        GuiBackendConfiguration guiConfig,
        AccountCache cache) {
        logger.info("Created AccountEventListener");
        return new AccountEventListener(rlnClient, guiConfig.partyDamlId(), cache);
    }

    @Dependent
    @Produces
    public AutoApproveEventListener getAutoApproveEventListener(RLNClient rlnClient,
        GuiBackendConfiguration guiConfig,
        AutoApproveCache cache) {
        logger.info("Created AutoApproveEventListener");
        return new AutoApproveEventListener(rlnClient, guiConfig.partyDamlId(), cache);
    }


    @ApplicationScoped
    @Produces
    public AccountCache getAccountCache() {
        logger.info("Created AccountCache");
        return new AccountCache();
    }

    @ApplicationScoped
    @Produces
    public AutoApproveCache getAutoApproveCache() {
        logger.info("Created AutoApproveCache");
        return new AutoApproveCache();
    }

    @ApplicationScoped
    @Produces
    public LiquidBalanceCache getLiquidBalanceCache() {
        logger.info("Created LiquidBalanceCache");
        return new LiquidBalanceCache();
    }

    @ApplicationScoped
    public LockedBalanceCache getLockedBalanceCache() {
        logger.info("Created LockedBalanceCache");
        return new LockedBalanceCache();
    }

    @ApplicationScoped
    @Produces
    public IncomingBalanceCache getIncomingBalanceCache() {
        logger.info("Created IncomingBalanceCache");
        return new IncomingBalanceCache();
    }
}
