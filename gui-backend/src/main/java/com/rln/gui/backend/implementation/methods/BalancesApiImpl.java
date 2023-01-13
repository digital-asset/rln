/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.rln.client.damlClient.ArchiveBalanceParameters;
import com.rln.client.damlClient.RLNClient;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.IncomingBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LockedBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.data.BalanceType;
import com.rln.gui.backend.implementation.balanceManagement.exception.IbanNotFoundException;
import com.rln.gui.backend.implementation.balanceManagement.exception.NonZeroBalanceException;
import com.rln.gui.backend.model.Balance;
import com.rln.gui.backend.model.BalanceChange;
import com.rln.gui.backend.model.WalletAddressTestDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class BalancesApiImpl {

    private final LiquidBalanceCache liquidBalanceCache;
    private final IncomingBalanceCache incomingBalanceCache;
    private final LockedBalanceCache lockedBalanceCache;
    private final AccountCache accountCache;
    private final RLNClient rlnClient;

    public BalancesApiImpl(
        LiquidBalanceCache liquidBalanceCache,
        IncomingBalanceCache incomingBalanceCache,
        LockedBalanceCache lockedBalanceCache,
        AccountCache accountCache,
        RLNClient rlnClient) {
        this.liquidBalanceCache = liquidBalanceCache;
        this.incomingBalanceCache = incomingBalanceCache;
        this.lockedBalanceCache = lockedBalanceCache;
        this.accountCache = accountCache;
        this.rlnClient = rlnClient;
    }

    /**
     *  walletId should be matched to existing iban of current bank
     *  return different types of balance following below:
     *      LIQUID: liquid
     *      ACTUAL: liquid + locked
     *      FUTURE: liquid + incoming
     */
    public List<Balance> getAddressBalance(String address) throws IbanNotFoundException {
        var liquidBalanceAmount = liquidBalanceCache
                .getBalance(address)
                .orElseThrow(() -> new IbanNotFoundException(address));
        var incomingBalanceAmount = incomingBalanceCache.getBalance(address);
        var lockedBalanceAmount = lockedBalanceCache.getBalance(address);

        var builder = Balance.builder()
            .address(address)
            .assetId(0L) // default to 0 now, as we don't have assetId in the system yet
            .assetName(accountCache.getAssetCode(address));

        var result = new ArrayList<Balance>(3);
        var liquidBalance = builder
            .balance(liquidBalanceAmount)
            .type(BalanceType.LIQUID.name()).build();
        result.add(liquidBalance);
        lockedBalanceAmount.ifPresent(actualLockedAmount -> {
            var actualBalance = builder
                .balance(liquidBalanceAmount.add(actualLockedAmount))
                .type(BalanceType.ACTUAL.name()).build();
            result.add(actualBalance);
        });
        incomingBalanceAmount.ifPresent(actualIncomingAmount -> {
            var futureBalance = builder
                .balance(liquidBalanceAmount.add(actualIncomingAmount))
                .type(BalanceType.FUTURE.name()).build();
            result.add(futureBalance);
        });

        return result;
    }

    public void delete(String provider, String address) throws NonZeroBalanceException {
        var balances = getAddressBalance(address);
        var allBalancesAreZero= balances.stream()
            .allMatch(balance -> balance.getBalance().compareTo(BigDecimal.ZERO) == 0);
        if (!allBalancesAreZero) {
            throw new NonZeroBalanceException(address);
        }
        rlnClient.archiveBalance(new ArchiveBalanceParameters(provider, address));
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
