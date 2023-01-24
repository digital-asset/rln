/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.daml.extensions.testing.ledger.SandboxManager;
import com.daml.ledger.javaapi.data.ArchivedEvent;
import com.daml.ledger.javaapi.data.ContractId;
import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.Party;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.BalanceOwner;
import com.rln.damlCodegen.model.balance.IncomingBalance;
import com.rln.damlCodegen.model.balance.LockedBalance;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalanceTestUtil {

    public static final String BANK_PARTY = "bankParty";
    public static final String IBAN1 = "IBAN1";
    public static final String IBAN2 = "IBAN2";
    public static final String ASSET_CODE1 = "USD";
    public static final String ASSET_CODE2 = "NTD";
    private static final Logger logger = LoggerFactory.getLogger(BalanceTestUtil.class);
    private static final Random rand = new Random();

    public static ContractId populateBalance(double amount, String iban, String assetCode,
        SandboxManager sandbox, Party currentBank, Identifier balanceTemplate)
        throws InvalidProtocolBufferException {
        return populateBalance(amount, iban, assetCode,
            sandbox, currentBank, null, balanceTemplate);
    }

    public static ContractId populateBalance(double amount, String iban, String assetCode,
        SandboxManager sandbox, Party currentBank, String owner, Identifier balanceTemplate)
            throws InvalidProtocolBufferException {
        var liquidBalance = createBalance(iban, currentBank.getValue(), owner, assetCode, amount, balanceTemplate);
        sandbox.getLedgerAdapter().createContract(currentBank, balanceTemplate, liquidBalance);
        // If there is an owner, we want to consume this contract from their event queue
        if (owner != null) {
            sandbox.getLedgerAdapter().getCreatedContractId(new Party(owner), balanceTemplate,
                com.daml.ledger.javaapi.data.ContractId::new);
        }
        return sandbox.getLedgerAdapter().getCreatedContractId(currentBank, balanceTemplate,
            com.daml.ledger.javaapi.data.ContractId::new);
    }

    public static ArchivedEvent archiveBalanceEvent(String cid, Identifier templateId) {
        ArchivedEvent event = Mockito.mock(ArchivedEvent.class);
        Mockito.when(event.getTemplateId()).thenReturn(templateId);
        Mockito.when(event.getContractId()).thenReturn(cid);

        return event;
    }

    public static CreatedEvent createBalanceEvent(String iban, String provider, String currency,
                                                  Double amount, String cid, Identifier templateId) {
        CreatedEvent event = Mockito.mock(CreatedEvent.class);
        Mockito.when(event.getContractId()).thenReturn(cid);
        Mockito.when(event.getTemplateId()).thenReturn(templateId);
        Mockito.when(event.getArguments()).thenReturn(createBalance(iban, provider, null, currency, amount, templateId));
        return event;
    }

    public static CreatedEvent createBalanceEvent(String iban, String provider, String currency, Double amount) {
        return createBalanceEvent(iban, provider, currency, amount, "anyCid", Balance.TEMPLATE_ID);
    }

    public static DamlRecord createBalance(String iban, String provider, String owner, String currency, Double amount, Identifier templateId) {
        Balance balance = new Balance(iban, provider, new BalanceOwner(owner, Optional.of(owner)), currency, BigDecimal.valueOf(amount));
        String context = generateRandomString(8);
        logger.info("===================== context {}", context);

        if (templateId.equals(Balance.TEMPLATE_ID)) {
            return balance.toValue();
        } else if (templateId.equals(LockedBalance.TEMPLATE_ID)) {
            return new LockedBalance(balance, context).toValue();
        } else {
            return new IncomingBalance(balance, context).toValue();
        }
    }

    public static Balance createBalance(String iban, String provider, String currency, Double amount) {
        return Balance.fromValue(createBalance(iban, provider, null, currency, amount, Balance.TEMPLATE_ID));
    }

    private static String generateRandomString(int length) {
        byte[] bytes = new byte[length];
        rand.nextBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
