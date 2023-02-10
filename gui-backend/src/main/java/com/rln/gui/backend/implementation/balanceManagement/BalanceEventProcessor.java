/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.daml.ledger.javaapi.data.*;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.IncomingBalance;
import com.rln.damlCodegen.model.balance.LockedBalance;
import com.rln.gui.backend.implementation.balanceManagement.cache.IncomingBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LockedBalanceCache;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalanceEventProcessor extends MessageProcessor<Event> {

    private static final Logger logger = LoggerFactory.getLogger(BalanceEventProcessor.class);

    private final LiquidBalanceCache liquidBalanceCache;
    private final LockedBalanceCache lockedBalanceCache;
    private final IncomingBalanceCache incomingBalanceCache;

    public BalanceEventProcessor(LiquidBalanceCache liquidBalanceCache,
                                 LockedBalanceCache lockedBalanceCache,
                                 IncomingBalanceCache incomingBalanceCache) {
        this.liquidBalanceCache = liquidBalanceCache;
        this.lockedBalanceCache = lockedBalanceCache;
        this.incomingBalanceCache = incomingBalanceCache;
    }

    @Override
    public void updateCache(Event allTypeBalanceEvent) {
        boolean isArchived = allTypeBalanceEvent instanceof ArchivedEvent;
        if (isArchived) {
            updateCache((ArchivedEvent) allTypeBalanceEvent);
        } else {
            updateCache((CreatedEvent) allTypeBalanceEvent);
        }
    }

    private void updateCache(ArchivedEvent event) {
        logger.info("Received archived event, update balance cache {}", event);
        ContractId cid = new ContractId(event.getContractId());
        Identifier templateId = event.getTemplateId();
        if (templateId.equals(Balance.TEMPLATE_ID)) {
            liquidBalanceCache.updateBalanceUponArchived(cid);
        } else if (templateId.equals(LockedBalance.TEMPLATE_ID)) {
            lockedBalanceCache.updateBalanceUponArchived(cid);
        } else if (templateId.equals(IncomingBalance.TEMPLATE_ID)) {
            incomingBalanceCache.updateBalanceUponArchived(cid);
        }
    }

    private void updateCache(CreatedEvent event) {
        logger.info("Received create event, update balance cache {}", event);
        ContractId cid = new ContractId(event.getContractId());
        DamlRecord balance = event.getArguments();
        Identifier templateId = event.getTemplateId();
        if (templateId.equals(Balance.TEMPLATE_ID)) {
            liquidBalanceCache.updateBalanceUponCreation(Balance.fromValue(balance), cid);
        } else if (templateId.equals(LockedBalance.TEMPLATE_ID)) {
            lockedBalanceCache.updateBalanceUponCreation(LockedBalance.fromValue(balance), cid);
        } else if (templateId.equals(IncomingBalance.TEMPLATE_ID)) {
            incomingBalanceCache.updateBalanceUponCreation(IncomingBalance.fromValue(balance), cid);
        }
    }
}
