/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;


import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Event;
import com.rln.client.damlClient.RLNClient;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.exception.IbanNotFoundException;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Consumer;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class AccountCacheTest {
    private static final RLNClient rlnClient = Mockito.mock(RLNClient.class);

    @Test
    void GIVEN_balances_on_ledger_WHEN_account_cache_init_THEN_accounts_info_stored_correctly() {
        // GIVEN
        var event1 = BalanceTestUtil.createBalanceEvent(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, 100.0);
        var event2 = BalanceTestUtil.createBalanceEvent(BalanceTestUtil.IBAN2, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE2, 100.0);


        Mockito.doAnswer(invocation -> {
            Consumer<Event> callback = invocation.getArgument(2);
            return Flowable.just(event1, event2).forEach(callback);
        }).when(rlnClient).subscribeForContinuousEvent(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());

        // WHEN
        AccountCache cache = new AccountCache();
        new AccountEventListener(rlnClient, BalanceTestUtil.BANK_PARTY, cache);

        // THEN
        MatcherAssert.assertThat(getAssetCode(cache, BalanceTestUtil.IBAN1), Matchers.is(BalanceTestUtil.ASSET_CODE1));
        MatcherAssert.assertThat(getAssetCode(cache, BalanceTestUtil.IBAN2), Matchers.is(BalanceTestUtil.ASSET_CODE2));
    }

    private String getAssetCode(AccountCache cache, String iban) {
        return cache.getAssetCode(iban)
            .orElseThrow(() -> new RuntimeException("Test error, IBAN not in the account cache: " + iban));
    }

    @Test
    void GIVEN_account_cached_WHEN_balance_with_new_account_created_THEN_new_accounts_cached() {
        // GIVEN
        var event1 = BalanceTestUtil.createBalanceEvent(BalanceTestUtil.IBAN1, BalanceTestUtil.BANK_PARTY, BalanceTestUtil.ASSET_CODE1, 100.0);
        ConnectableFlowable<CreatedEvent> balances = Flowable.just(event1).publish();
        Mockito.doAnswer(invocation -> {
            Consumer<Event> callback = invocation.getArgument(2);
            return balances.forEach(callback);
        }).when(rlnClient).subscribeForContinuousEvent(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());

        AccountCache cache = new AccountCache();
        new AccountEventListener(rlnClient, BalanceTestUtil.BANK_PARTY, cache);

        MatcherAssert.assertThat(cache.getAssetCode(BalanceTestUtil.IBAN1), Matchers.is(Optional.empty()));

        // WHEN
        balances.connect();

        // THEN
        MatcherAssert.assertThat(getAssetCode(cache, BalanceTestUtil.IBAN1), Matchers.is(BalanceTestUtil.ASSET_CODE1));
    }
}
