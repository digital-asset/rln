/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.IncomingBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LockedBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.data.BalanceType;
import com.rln.gui.backend.model.Balance;
import com.rln.gui.backend.model.BalanceChange;
import com.rln.gui.backend.model.WalletAddressTestDTO;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BalancesApiImpl {

    private final LiquidBalanceCache liquidBalanceCache;
    private final IncomingBalanceCache incomingBalanceCache;
    private final LockedBalanceCache lockedBalanceCache;
    private final AccountCache accountCache;

    public BalancesApiImpl(
        LiquidBalanceCache liquidBalanceCache,
        IncomingBalanceCache incomingBalanceCache,
        LockedBalanceCache lockedBalanceCache,
        AccountCache accountCache) {
        this.liquidBalanceCache = liquidBalanceCache;
        this.incomingBalanceCache = incomingBalanceCache;
        this.lockedBalanceCache = lockedBalanceCache;
        this.accountCache = accountCache;
    }

    /**
     *  walletId should be matched to existing iban of current bank
     *  return different types of balance following below:
     *      LIQUID: liquid
     *      ACTUAL: liquid + locked
     *      FUTURE: liquid + incoming
     */
    public List<Balance> getAddressBalance(String address) {
        var liquidBalanceAmount = liquidBalanceCache.getBalance().get(address);
        var incomingBalanceAmount = incomingBalanceCache.getBalance().get(address);
        var lockedBalanceAmount = lockedBalanceCache.getBalance().get(address);

        var builder = Balance.builder()
            .address(address)
            .assetId(0L) // default to 0 now, as we don't have assetId in the system yet
            .assetName(accountCache.getAssetCode(address));

        var actualBalance = builder
            .balance(liquidBalanceAmount.add(lockedBalanceAmount))
            .type(BalanceType.ACTUAL.name()).build();

        var liquidBalance = builder
            .balance(liquidBalanceAmount)
            .type(BalanceType.LIQUID.name()).build();

        var futureBalance = builder
            .balance(liquidBalanceAmount.add(incomingBalanceAmount))
            .type(BalanceType.FUTURE.name()).build();

        return List.of(actualBalance, liquidBalance, futureBalance);
    }

    public List<Balance> changeBalance(String address, @Valid @NotNull BalanceChange balanceChange) {
        return null;
    }

    public List<Balance> getBalances(Long walletId) {
        return null;
    }

    public List<Balance> getLocalBalance(String address) {
        return null;
    }

    public Object testWalletAddress(String address,
        @Valid @NotNull WalletAddressTestDTO walletAddressTestDTO) {
        return null;
    }
}
