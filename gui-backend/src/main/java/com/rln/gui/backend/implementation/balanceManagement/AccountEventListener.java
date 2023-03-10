/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.daml.ledger.javaapi.data.ArchivedEvent;
import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Event;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.codegen.ValueDecoder;
import com.rln.client.damlClient.RLNClient;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountEventListener {

    private static final ValueDecoder<Balance> BALANCE_DECODER = Balance.valueDecoder();
    private static final Logger logger = LoggerFactory.getLogger(AccountEventListener.class);
    private static final Set<Identifier> accountTemplates = Set.of(Balance.TEMPLATE_ID);
    private final AccountCache accountCache;

    public AccountEventListener(RLNClient rlnClient, String bankParty, AccountCache accountCache) {
        this.accountCache = accountCache;
        rlnClient.subscribeForContinuousEvent(bankParty, accountTemplates, this::updateAccountWithEvent);
    }

    private void updateAccountWithEvent(Event event) {
        logger.info("Update Account with {}", event);
        if (event instanceof ArchivedEvent) {
            accountCache.delete(event.getContractId());
        } else {
            var createdEvent = (CreatedEvent) event;
            var accountInfo = new AccountInfo(BALANCE_DECODER.decode(createdEvent.getArguments()));
            accountCache.update(createdEvent.getContractId(), accountInfo);
        }
    }
}
