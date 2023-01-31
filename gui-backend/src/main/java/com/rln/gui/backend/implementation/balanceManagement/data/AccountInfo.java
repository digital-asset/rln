/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.data;

import com.rln.damlCodegen.model.balance.Balance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class AccountInfo {

    private final String ownerName;
    private String providerParty;
    private String ownerParty;
    private String iban;
    private String assetCode;

    public AccountInfo(Balance balance) {
        providerParty = balance.provider;
        iban = balance.iban;
        assetCode = balance.currency;
        Objects.requireNonNull(balance.owner);
        ownerParty = balance.owner.party.orElse(null);
        ownerName = balance.owner.name;
    }

    /**
     * P   O  = A
     * !P  O = A
     * P  !O = L
     * !P !O = L
     * with P = provider, O = Owner, A = Asset, L = Liability
     *
     * @param party party
     * @return true if asset belongs to party
     */
    public boolean isAssetOf(String party) {
        return party.equals(ownerParty);
    }
}
