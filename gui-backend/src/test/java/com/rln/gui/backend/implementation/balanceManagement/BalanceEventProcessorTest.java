/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.daml.ledger.javaapi.data.ArchivedEvent;
import com.daml.ledger.javaapi.data.ContractId;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.IncomingBalance;
import com.rln.damlCodegen.model.balance.LockedBalance;
import com.rln.gui.backend.implementation.balanceManagement.cache.IncomingBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LockedBalanceCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BalanceEventProcessorTest {

    private static LiquidBalanceCache liquidBalanceCache;
    private static LockedBalanceCache lockedBalanceCache;
    private static IncomingBalanceCache incomingBalanceCache;

    private static BalanceEventProcessor processor;

    @BeforeEach
    void setup () {
        liquidBalanceCache =  Mockito.mock(LiquidBalanceCache.class);
        lockedBalanceCache =  Mockito.mock(LockedBalanceCache.class);
        incomingBalanceCache =  Mockito.mock(IncomingBalanceCache.class);

        processor = new BalanceEventProcessor(liquidBalanceCache, lockedBalanceCache, incomingBalanceCache);
    }

    @Test
    void GIVEN_create_and_archive_liquid_balance_WHEN_process_THEN_cache_update_successfully() {
        // GIVEN
        String cid = "liquid balance cid";
        var liquidBalanceCreateEvent =
                BalanceTestUtil.createBalanceEvent(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, 100.0, cid, Balance.TEMPLATE_ID);

        // WHEN
        processor.updateCache(liquidBalanceCreateEvent);

        // THEN
        Balance balance = Balance.fromValue(liquidBalanceCreateEvent.getArguments());
        Mockito.verify(liquidBalanceCache).updateBalanceUponCreation(balance, new ContractId(cid));
        Mockito.verifyNoInteractions(lockedBalanceCache);
        Mockito.verifyNoInteractions(incomingBalanceCache);

        // GIVEN
        ArchivedEvent archivedEvent = BalanceTestUtil.archiveBalanceEvent(cid, Balance.TEMPLATE_ID);

        // WHEN
        processor.updateCache(archivedEvent);

        // THEN
        Mockito.verify(liquidBalanceCache).updateBalanceUponArchived(new ContractId(cid));
        Mockito.verifyNoInteractions(lockedBalanceCache);
        Mockito.verifyNoInteractions(incomingBalanceCache);
    }

    @Test
    void GIVEN_create_and_archive_locked_balance_WHEN_process_THEN_cache_update_successfully() {
        // GIVEN
        String cid = "locked balance cid";
        var lockedBalanceEvent =
                BalanceTestUtil.createBalanceEvent(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, 100.0, cid, LockedBalance.TEMPLATE_ID);

        // WHEN
        processor.updateCache(lockedBalanceEvent);

        // THEN
        LockedBalance balance = LockedBalance.fromValue(lockedBalanceEvent.getArguments());
        Mockito.verify(lockedBalanceCache).updateBalanceUponCreation(balance, new ContractId(cid));
        Mockito.verifyNoInteractions(liquidBalanceCache);
        Mockito.verifyNoInteractions(incomingBalanceCache);

        // GIVEN
        ArchivedEvent archivedEvent = BalanceTestUtil.archiveBalanceEvent(cid, LockedBalance.TEMPLATE_ID);

        // WHEN
        processor.updateCache(archivedEvent);

        // THEN
        Mockito.verify(lockedBalanceCache).updateBalanceUponArchived(new ContractId(cid));
        Mockito.verifyNoInteractions(liquidBalanceCache);
        Mockito.verifyNoInteractions(incomingBalanceCache);

    }

    @Test
    void GIVEN_create_and_archive_incoming_balance_WHEN_process_THEN_cache_update_successfully() {
        // GIVEN
        String cid = "incoming balance cid";
        var incomingBalanceEvent =
                BalanceTestUtil.createBalanceEvent(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, 100.0, cid, IncomingBalance.TEMPLATE_ID);

        // WHEN
        processor.updateCache(incomingBalanceEvent);

        // THEN
        IncomingBalance balance = IncomingBalance.fromValue(incomingBalanceEvent.getArguments());
        Mockito.verify(incomingBalanceCache).updateBalanceUponCreation(balance, new ContractId(cid));
        Mockito.verifyNoInteractions(lockedBalanceCache);
        Mockito.verifyNoInteractions(liquidBalanceCache);

        // GIVEN
        ArchivedEvent archivedEvent = BalanceTestUtil.archiveBalanceEvent(cid, IncomingBalance.TEMPLATE_ID);

        // WHEN
        processor.updateCache(archivedEvent);

        // THEN
        Mockito.verify(incomingBalanceCache).updateBalanceUponArchived(new ContractId(cid));
        Mockito.verifyNoInteractions(lockedBalanceCache);
        Mockito.verifyNoInteractions(liquidBalanceCache);

    }
}
