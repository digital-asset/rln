/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.daml.ledger.javaapi.data.ContractId;
import com.daml.ledger.javaapi.data.Template;
import com.rln.gui.backend.implementation.balanceManagement.exception.ContractIdNotFoundException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * We have totally 3 maps to keep track of the balances
 * 1. Iban (account) to contractIds mapping (one account can have many locked and incoming balances)
 * 2. contractId to Iban (account) mapping (one contractId should map to only one account)
 * 3.contractId to Balance mapping (one to one)
 */
public abstract class BalanceCache<T extends Template> {

    protected final Map<ContractId, String> contractIdToIban = new ConcurrentHashMap<>();
    protected final Map<String, Set<ContractId>> ibanToContractIds = new ConcurrentHashMap<>();
    protected final Map<ContractId, T> contractIdToBalance = new ConcurrentHashMap<>();

    public void updateBalanceUponCreation(T balance, ContractId cid) {
        String iban = getIban(balance);
        contractIdToIban.put(cid, iban);
        ibanToContractIds.putIfAbsent(iban, new HashSet<>());
        ibanToContractIds.computeIfPresent(iban, (key, currentSet) -> {
            currentSet.add(cid);
            return currentSet;
        });
        contractIdToBalance.put(cid, balance);
    }

    public void updateBalanceUponArchived(ContractId cid) {
        var iban = contractIdToIban.remove(cid);
        contractIdToBalance.remove(cid);
        if (iban == null) {
            throw new ContractIdNotFoundException(cid);
        }
        ibanToContractIds.computeIfPresent(iban, (k, v) -> {
            v.remove(cid);
            return v;
        });
    }

    // map from iban to total balanceAmount
    public Optional<BigDecimal> getBalance(String address) {
        return Optional.ofNullable(ibanToContractIds.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(cid -> {
                                    var balance = contractIdToBalance.get(cid);
                                    if (balance == null) {
                                        throw new ContractIdNotFoundException(cid);
                                    }
                                    return getBalanceAmount(balance);
                                }).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .get(address));
    }

    abstract protected String getIban(T balance);

    abstract protected BigDecimal getBalanceAmount(T balance);
}
