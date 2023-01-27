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
import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import com.rln.gui.backend.implementation.balanceManagement.data.BalanceType;
import com.rln.gui.backend.implementation.balanceManagement.exception.IbanNotFoundException;
import com.rln.gui.backend.implementation.balanceManagement.exception.NonZeroBalanceException;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.model.Balance;
import com.rln.gui.backend.model.BalanceChange;
import com.rln.gui.backend.model.WalletAddressTestDTO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    public BalancesApiImpl(
            GuiBackendConfiguration guiBackendConfiguration,
            LiquidBalanceCache liquidBalanceCache,
            IncomingBalanceCache incomingBalanceCache,
            LockedBalanceCache lockedBalanceCache,
            AccountCache accountCache,
            RLNClient rlnClient) {
        this.guiBackendConfiguration = guiBackendConfiguration;
        this.liquidBalanceCache = liquidBalanceCache;
        this.incomingBalanceCache = incomingBalanceCache;
        this.lockedBalanceCache = lockedBalanceCache;
        this.accountCache = accountCache;
        this.rlnClient = rlnClient;
    }

    /**
     * walletId should be matched to existing iban of current bank
     * return different types of balance following below:
     * LIQUID: liquid
     * ACTUAL: liquid + locked
     * FUTURE: liquid + incoming
     */
    public List<Balance> getAddressBalance(String address) throws IbanNotFoundException {
        var liquidBalanceAmount = liquidBalanceCache
                .getBalance(address)
                .orElseThrow(() -> new IbanNotFoundException(address));
        var incomingBalanceAmount = incomingBalanceCache.getBalance(address);
        var lockedBalanceAmount = lockedBalanceCache.getBalance(address);
        var assetName = accountCache
                .getAccountInfo(address)
                .map(AccountInfo::getAssetCode)
                .orElseThrow(() -> new IbanNotFoundException(address));

        var builder = Balance.builder()
                .address(address)
                .assetId(ASSET_ID) // default to 0 now, as we don't have assetId in the system yet
                .assetName(assetName);

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

    public List<Balance> getLocalBalance(String address) throws IbanNotFoundException {
        var accountInfo = accountCache.getAccountInfo(address);
        return accountInfo
                .filter(isLocal())
                .map(info -> getAddressBalance(address))
                .orElseThrow(() -> new IbanNotFoundException(address));
    }

    private Predicate<AccountInfo> isLocal() {
        return info -> info.getProvider().equals(guiBackendConfiguration.partyDamlId());
    }

    public void delete(String provider, String address) throws NonZeroBalanceException {
        var balances = getAddressBalance(address);
        var allBalancesAreZero = balances.stream()
                .allMatch(balance -> balance.getBalance().compareTo(BigDecimal.ZERO) == 0);
        if (!allBalancesAreZero) {
            throw new NonZeroBalanceException(address);
        }
        rlnClient.archiveBalance(new ArchiveBalanceParameters(provider, address));
    }

    public List<Balance> changeBalance(String provider, String address, @Valid @NotNull BalanceChange balanceChange) {
        rlnClient.changeBalance(new ChangeBalanceParameters(provider, address, balanceChange.getChange()));
        return getAddressBalance(address)
                .stream().map(balance -> {
                    if (BalanceType.LIQUID.toString().equals(balance.getType())) {
                        balance.setBalance(balance.getBalance().add(balanceChange.getChange()));
                    }
                    return balance;
                }).collect(Collectors.toList());
    }

    public List<Balance> getBalances(Long walletId) {
        if (!walletId.equals(guiBackendConfiguration.partyId()))
            throw new IllegalArgumentException();

        return accountCache.getAccounts()
                .stream()
                .map(this::getAddressBalance)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private Balance toBalance(com.rln.damlCodegen.model.balance.Balance contract) {
        var assetName = accountCache
                .getAccountInfo(contract.iban)
                .map(AccountInfo::getAssetCode)
                .orElseThrow(() -> new IbanNotFoundException(contract.iban));

        return Balance.builder()
                .address(contract.iban)
                .assetId(ASSET_ID) // default to 0 now, as we don't have assetId in the system yet
                .assetName(assetName)
                .build();
    }

}
