/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.daml.ledger.javaapi.data.ContractId;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.gui.backend.implementation.balanceManagement.cache.BalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.exception.ContractIdNotFoundException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

class BalanceCacheTest {

    @Test
    void GIVEN_several_balance_created_events_WHEN_update_cache_THEN_balance_aggregate_correctly() {
        // GIVEN
        double amount1InAccount1 = 100.0;
        double amount2InAccount1 = 200.0;
        double amountInAccount2 = 450;
        var balance1Account1 = BalanceTestUtil.createBalance(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, amount1InAccount1);
        var balance2Account1 = BalanceTestUtil.createBalance(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, amount2InAccount1);
        var balanceAccount2 = BalanceTestUtil.createBalance(BalanceTestUtil.IBAN2, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE2, amountInAccount2);
        ContractId cid1 = new ContractId("balance1Account1");
        ContractId cid2 = new ContractId("balance2Account1");
        ContractId cid3 = new ContractId("balanceAccount2");

        // WHEN
        BalanceCache<Balance> cache = new LiquidBalanceCache();
        cache.updateBalanceUponCreation(balance1Account1, cid1);
        cache.updateBalanceUponCreation(balance2Account1, cid2);
        cache.updateBalanceUponCreation(balanceAccount2, cid3);

        // THEN
        MatcherAssert.assertThat(cache.getBalance(BalanceTestUtil.IBAN1).doubleValue(), Matchers.is(amount1InAccount1 + amount2InAccount1));
        MatcherAssert.assertThat(cache.getBalance(BalanceTestUtil.IBAN2).doubleValue(), Matchers.is(amountInAccount2));
    }

    @Test
    void GIVEN_balances_in_cache_WHEN_archive_event_comes_THEN_balance_deduct_correctly() {
        // GIVEN
        double amount = 100.0;
        var balance = BalanceTestUtil.createBalance(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, amount);
        ContractId cid = new ContractId("balance cid");
        BalanceCache<Balance> cache = new LiquidBalanceCache();
        cache.updateBalanceUponCreation(balance, cid);

        MatcherAssert.assertThat(cache.getBalance(BalanceTestUtil.IBAN1).doubleValue(), Matchers.is(amount));

        // WHEN existing balance cid archived
        cache.updateBalanceUponArchived(cid);
        MatcherAssert.assertThat(cache.getBalance(BalanceTestUtil.IBAN1).doubleValue(), Matchers.is(0.0d));

        // WHEN non-existing balance cid archived
        Assertions.assertThrows(ContractIdNotFoundException.class,
                () -> cache.updateBalanceUponArchived(new ContractId("non-existing cid")));
    }
}
