/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import com.rln.gui.backend.implementation.balanceManagement.exception.IbanNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AccountCache {

    private final Map<String, AccountInfo> accounts = new ConcurrentHashMap<>();

    public Set<Entry<String, AccountInfo>> getAccounts() {
        return accounts.entrySet();
    }

    public String getAssetCode(String iban) {
        if (!accounts.containsKey(iban)) {
            throw new IbanNotFoundException(iban);
        }
        return accounts.get(iban).getAssetCode();
    }

    public void update(String iban, AccountInfo accountInfo) {
        accounts.putIfAbsent(iban, accountInfo);
    }
}
