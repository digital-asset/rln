/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.rln.client.damlClient.ArchiveBalanceParameters;
import com.rln.client.damlClient.ChangeBalanceParameters;
import com.rln.client.damlClient.RLNClient;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.IncomingBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LockedBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.data.AccountBalance;
import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import com.rln.gui.backend.implementation.balanceManagement.exception.IbanNotFoundException;
import com.rln.gui.backend.implementation.balanceManagement.exception.NonZeroBalanceException;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.model.BalanceChange;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BalancesApiImpl {

    private static final Long ASSET_ID = 0L;

    private final GuiBackendConfiguration guiBackendConfiguration;
    private final LiquidBalanceCache liquidBalanceCache;
    private final IncomingBalanceCache incomingBalanceCache;
    private final LockedBalanceCache lockedBalanceCache;
    private final AccountCache accountCache;
    private final RLNClient rlnClient;
    private final SetlPartySupplier setlPartySupplier;

    public BalancesApiImpl(
            GuiBackendConfiguration guiBackendConfiguration,
            LiquidBalanceCache liquidBalanceCache,
            IncomingBalanceCache incomingBalanceCache,
            LockedBalanceCache lockedBalanceCache,
            AccountCache accountCache,
            RLNClient rlnClient,
            SetlPartySupplier setlPartySupplier) {
        this.guiBackendConfiguration = guiBackendConfiguration;
        this.liquidBalanceCache = liquidBalanceCache;
        this.incomingBalanceCache = incomingBalanceCache;
        this.lockedBalanceCache = lockedBalanceCache;
        this.accountCache = accountCache;
        this.rlnClient = rlnClient;
        this.setlPartySupplier = setlPartySupplier;
    }

    /**
     * walletId should be matched to existing iban of current bank
     * return different types of balance following below:
     * LIQUID: liquid
     * ACTUAL: liquid + locked
     * FUTURE: liquid + incoming
     */
    public AccountBalance getAddressBalance(String address) throws IbanNotFoundException {
        var liquidBalanceAmount = liquidBalanceCache
                .getBalance(address)
                .orElseThrow(() -> new IbanNotFoundException(address));
        var incomingBalanceAmount = incomingBalanceCache.getBalance(address).orElse(BigDecimal.ZERO);
        var lockedBalanceAmount = lockedBalanceCache.getBalance(address).orElse(BigDecimal.ZERO);
        var assetName = accountCache
                .getAccountInfo(address)
                .map(AccountInfo::getAssetCode)
                .orElseThrow(() -> new IbanNotFoundException(address));

        var accountInfo = accountCache.getAccountInfo(address).orElseThrow(() -> new IbanNotFoundException(address));
        var providerName = setlPartySupplier.getSetlParty(accountInfo.getProviderParty()).getName();
        return new AccountBalance(accountInfo,
                providerName,
                assetName,
                address,
                liquidBalanceAmount,
                incomingBalanceAmount,
                lockedBalanceAmount);
    }

    public AccountBalance getLocalBalance(String address) throws IbanNotFoundException {
        var accountInfo = accountCache.getAccountInfo(address);
        return accountInfo
                .filter(isLocal())
                .map(info -> getAddressBalance(address))
                .orElseThrow(() -> new IbanNotFoundException(address));
    }

    private Predicate<AccountInfo> isLocal() {
        return info -> info.getProviderParty().equals(guiBackendConfiguration.partyDamlId());
    }

    public void delete(String provider, String address) throws NonZeroBalanceException {
        var accountBalance = getAddressBalance(address);
        if (!accountBalance.isAllZero()) {
            throw new NonZeroBalanceException(address);
        }
        rlnClient.archiveBalance(new ArchiveBalanceParameters(provider, address));
    }

    public AccountBalance changeBalance(String provider, String address, @Valid @NotNull BalanceChange balanceChange) {
        var accountBalance = getAddressBalance(address);
        accountBalance.addLiquid(balanceChange.getChange());
        rlnClient.changeBalance(new ChangeBalanceParameters(provider, address, balanceChange.getChange()));
        return accountBalance;
    }

    /**
     * Lists balances of given wallet where the current party is either the owner or the provider
     *
     * @param walletId wallet
     * @return list of balances
     */
    public List<AccountBalance> getBalances(Long walletId) {
        return accountCache.getAccounts()
                .stream()
                .map(AccountInfo::getIban)
                .map(this::getAddressBalance)
                .collect(Collectors.toList());
    }


}
