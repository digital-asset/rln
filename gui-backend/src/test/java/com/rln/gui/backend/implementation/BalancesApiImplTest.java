/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation;


import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.IncomingBalance;
import com.rln.damlCodegen.model.balance.LockedBalance;
import com.rln.gui.backend.implementation.balanceManagement.AccountEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceTestUtil;
import com.rln.gui.backend.implementation.balanceManagement.data.BalanceType;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
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

        var balanceId = BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        var lockedId1 = BalanceTestUtil.populateBalance(lockedAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID);
        var lockedId2 = BalanceTestUtil.populateBalance(lockedAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID);

        var incomingId1 = BalanceTestUtil.populateBalance(incomingAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID);
        var incomingId2 = BalanceTestUtil.populateBalance(incomingAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID);

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured
                .get(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN1))
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

        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), Balance.TEMPLATE_ID, balanceId.getValue());
        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID, lockedId1
            .getValue());
        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID, lockedId2
            .getValue());
        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID, incomingId1
            .getValue());
        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID, incomingId2
            .getValue());
    }

    @Test
    void GIVEN_only_liquid_balance_on_ledger_WHEN_get_request_balance_endpoint_THEN_return_correct_balances() throws InvalidProtocolBufferException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        double liquidAmount = 100.0;
        var balanceId = BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured
            .get(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN1))
            .then()
            .statusCode(200)
            .extract().body().as(new TypeRef<>() {
            });

        // THEN
        MatcherAssert.assertThat(balances.get(0).getBalance().doubleValue(), Matchers.is(liquidAmount));

        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), Balance.TEMPLATE_ID, balanceId.getValue());
    }

    @Test
    void GIVEN_balance_not_on_ledger_WHEN_get_request_balance_endpoint_THEN_return_correct_balances() {
        RestAssured.get(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN2))
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
        RestAssured.delete(String.format("/api/ledger/addresses/%s", BalanceTestUtil.IBAN1))
            .then()
            .assertThat()
            .statusCode(204);
    }

    @Test
    void GIVEN_non_zero_liquid_balance_on_ledger_WHEN_get_request_delete_address_THEN_balance_not_deleted() throws InvalidProtocolBufferException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        double liquidAmount = 100.0;
        var balanceId = BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        RestAssured.delete(String.format("/api/ledger/addresses/%s", BalanceTestUtil.IBAN1))
            .then()
            .assertThat()
            .statusCode(403);
        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), Balance.TEMPLATE_ID, balanceId.getValue());
    }

    @Test
    void GIVEN_balances_on_ledger_WHEN_post_change_balance_endpoint_THEN_return_correct_balances() throws InvalidProtocolBufferException {
        double liquidAmount = 100.0;

        BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured.given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(createBalanceChangeRequest("1", USD, liquidAmount))
            .when().post(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN1))
            .then()
            .statusCode(200)
            .extract().body().as(new TypeRef<>() {
            });

        // THEN
        MatcherAssert.assertThat(balances.get(0).getBalance().doubleValue(), Matchers.is(2 * liquidAmount));

        var newLiquidBalance = SANDBOX.getLedgerAdapter()
            .getCreatedContractId(getCurrentBankPartyId(), Balance.TEMPLATE_ID, com.daml.ledger.javaapi.data.ContractId::new);
        TransactionsApiImplTest.cleanupContract(getCurrentBankPartyId(), Balance.TEMPLATE_ID, newLiquidBalance.getValue());
    }

    private Map<String, Object> createBalanceChangeRequest(String assetId, String addetName, double change) {
        Map<String, Object> transferProposalRequest = new HashMap<>();
        transferProposalRequest.put("assetId", assetId);
        transferProposalRequest.put("assetName", addetName);
        transferProposalRequest.put("change", change);
        return transferProposalRequest;
    }
}
