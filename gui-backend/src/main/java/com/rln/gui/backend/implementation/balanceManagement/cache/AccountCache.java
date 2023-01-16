/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AccountCache {

    private final Map<String, AccountInfo> accounts = new ConcurrentHashMap<>();
    private final Map<String, String> balanceCidToAddress = new ConcurrentHashMap<>();

    public Set<String> getAccounts() {
        return accounts.keySet();
    }

    public Optional<AccountInfo> getAccountInfo(String iban) {
        return Optional.ofNullable(accounts.get(iban));
    }

    public void delete(String contractId) {
        // TODO error handling
        var iban = balanceCidToAddress.get(contractId);
        balanceCidToAddress.remove(contractId);
        if (iban != null) {
            accounts.remove(iban);
        }
    }

    public void update(String contractId, String iban, AccountInfo accountInfo) {
        // TODO error handling
        balanceCidToAddress.putIfAbsent(contractId, iban);
        accounts.putIfAbsent(iban, accountInfo);
    }
}
