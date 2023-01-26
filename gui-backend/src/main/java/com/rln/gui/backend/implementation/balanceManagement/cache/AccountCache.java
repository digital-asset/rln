/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AccountCache {

    private final Map<String, AccountInfo> accounts = new ConcurrentHashMap<>();
    private final Map<String, String> balanceCidToAddress = new ConcurrentHashMap<>();

    public Collection<AccountInfo> getAccounts() {
        return accounts.values();
    }

    public Optional<AccountInfo> getAccountInfo(String iban) {
        return Optional.ofNullable(accounts.get(iban));
    }

    public void delete(String contractId) {
        var iban = balanceCidToAddress.remove(contractId);
        if (iban != null) {
            accounts.remove(iban);
        }
    }

    public void update(String contractId, AccountInfo accountInfo) {
        balanceCidToAddress.putIfAbsent(contractId, accountInfo.getIban());
        accounts.putIfAbsent(accountInfo.getIban(), accountInfo);
    }
}
