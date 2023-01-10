/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.rln.damlCodegen.model.balance.Balance;

import java.math.BigDecimal;

public class LiquidBalanceCache extends BalanceCache<Balance> {

    @Override
    protected String getIban(Balance balance) {
        return balance.iban;
    }

    @Override
    protected BigDecimal getBalanceAmount(Balance balance) {
        return balance.amount;
    }
}
