/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.rln.damlCodegen.model.balance.IncomingBalance;

import java.math.BigDecimal;

public class IncomingBalanceCache extends BalanceCache<IncomingBalance> {

    @Override
    protected String getIban(IncomingBalance balance) {
        return balance.balance.iban;
    }

    @Override
    protected BigDecimal getBalanceAmount(IncomingBalance balance) {
        return balance.balance.amount;
    }
}
