/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.daml.ledger.javaapi.data.Identifier;
import com.rln.client.damlClient.RLNClient;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.IncomingBalance;
import com.rln.damlCodegen.model.balance.LockedBalance;

import java.util.Set;

public class BalanceEventListener {

    private static final Set<Identifier> balanceTemplates =
            Set.of(Balance.TEMPLATE_ID, LockedBalance.TEMPLATE_ID, IncomingBalance.TEMPLATE_ID);

    public BalanceEventListener(RLNClient rlnClient, BalanceEventProcessor processor, String bankParty) {
        rlnClient.subscribeForContinuousEvent(bankParty, balanceTemplates, processor::updateCache);
    }
}
