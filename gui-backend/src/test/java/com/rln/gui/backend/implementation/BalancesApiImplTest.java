/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation;


import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.IncomingBalance;
import com.rln.damlCodegen.model.balance.LockedBalance;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.gui.backend.implementation.GuiBackendTest;
import com.rln.gui.backend.implementation.LedgerBaseTest;
import com.rln.gui.backend.implementation.balanceManagement.AccountEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceTestUtil;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.data.BalanceType;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import javax.inject.Inject;
import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@TestProfile(GuiBackendTest.class)
@QuarkusTest
class BalancesApiImplTest extends LedgerBaseTest {

    @Inject
    AccountEventListener accountEventListener;

    @Inject
    BalanceEventListener balanceEventListener;

    @Test
    void GIVEN_balances_on_ledger_WHEN_get_request_balance_endpoint_THEN_return_correct_balances() throws InvalidProtocolBufferException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        double liquidAmount = 100.0;
        double lockedAmount = 200.0;
        double incomingAmount = 300.0;

        var c1 = BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        var c2 = BalanceTestUtil.populateBalance(lockedAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID);
        var c3 = BalanceTestUtil.populateBalance(lockedAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID);

        var c4 = BalanceTestUtil.populateBalance(incomingAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID);
        var c5 = BalanceTestUtil.populateBalance(incomingAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID);

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured.given()
                .when().get(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN1))
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<>() {
                });

        // THEN
        for (var balance : balances) {
            if (balance.getType().equals(BalanceType.LIQUID.name())) {
                MatcherAssert.assertThat(balance.getBalance().doubleValue(), Matchers.is(liquidAmount));
            } else if (balance.getType().equals(BalanceType.ACTUAL.name())) {
                MatcherAssert.assertThat(balance.getBalance().doubleValue(), Matchers.is(liquidAmount + 2 * lockedAmount));
            } else if (balance.getType().equals(BalanceType.FUTURE.name())) {
                MatcherAssert.assertThat(balance.getBalance().doubleValue(), Matchers.is((liquidAmount + 2 * incomingAmount)));
            }
        }

        TransactionsApiImplTest.cleanup(getCurrentBankPartyId(), Balance.TEMPLATE_ID, c1.getValue());
        TransactionsApiImplTest.cleanup(getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID, c2.getValue());
        TransactionsApiImplTest.cleanup(getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID, c3.getValue());
        TransactionsApiImplTest.cleanup(getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID, c4.getValue());
        TransactionsApiImplTest.cleanup(getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID, c5.getValue());
    }

    @Test
    void GIVEN_only_liquid_balance_on_ledger_WHEN_get_request_balance_endpoint_THEN_return_correct_balances() throws InvalidProtocolBufferException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        double liquidAmount = 100.0;
        var c1 = BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured.given()
            .when().get(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN1))
            .then()
            .statusCode(200)
            .extract().body().as(new TypeRef<>() {
            });

        // THEN
        MatcherAssert.assertThat(balances.get(0).getBalance().doubleValue(), Matchers.is(liquidAmount));

        TransactionsApiImplTest.cleanup(getCurrentBankPartyId(), Balance.TEMPLATE_ID, c1.getValue());
    }

    @Test
    void GIVEN_balance_not_on_ledger_WHEN_get_request_balance_endpoint_THEN_return_correct_balances() {
        RestAssured.given()
            .when().get(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN2))
            .then()
            .assertThat()
            .statusCode(404);
    }

    @Test
    void GIVEN_only_zero_liquid_balance_on_ledger_WHEN_get_request_delete_address_THEN_balance_deleted() throws InvalidProtocolBufferException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        double liquidAmount = 0.0;
        BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        RestAssured.given()
            .when().delete(String.format("/api/ledger/addresses/%s", BalanceTestUtil.IBAN1))
            .then()
            .assertThat()
            .statusCode(204);
    }

    @Test
    void GIVEN_non_zero_liquid_balance_on_ledger_WHEN_get_request_delete_address_THEN_balance_not_deleted() throws InvalidProtocolBufferException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        double liquidAmount = 100.0;
        var c1 = BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        RestAssured.given()
            .when().delete(String.format("/api/ledger/addresses/%s", BalanceTestUtil.IBAN1))
            .then()
            .assertThat()
            .statusCode(403);
        TransactionsApiImplTest.cleanup(getCurrentBankPartyId(), Balance.TEMPLATE_ID, c1.getValue());
    }
}
