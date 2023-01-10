/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.rln.damlCodegen.model.balance.LockedBalance;

import java.math.BigDecimal;

public class LockedBalanceCache extends BalanceCache<LockedBalance> {

    @Override
    protected String getIban(LockedBalance balance) {
        return balance.balance.iban;
    }

    @Override
    protected BigDecimal getBalanceAmount(LockedBalance balance) {
        return balance.balance.amount;
    }
}
