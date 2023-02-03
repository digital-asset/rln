/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;


import com.daml.ledger.javaapi.data.ContractId;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.IncomingBalance;
import com.rln.damlCodegen.model.balance.LockedBalance;
import com.rln.gui.backend.implementation.balanceManagement.AccountEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceEventListener;
import com.rln.gui.backend.implementation.balanceManagement.BalanceTestUtil;
import com.rln.gui.backend.implementation.balanceManagement.data.BalanceType;
import com.rln.gui.backend.implementation.config.SetlParty;
import com.rln.gui.backend.implementation.profiles.GuiBackendTestProfile;
import com.rln.gui.backend.test.util.Util;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestProfile(GuiBackendTestProfile.class)
@QuarkusTest
class BalancesApiImplTest extends LedgerBaseTest {

    @Inject
    AccountEventListener accountEventListener;

    @Inject
    BalanceEventListener balanceEventListener;

    @InjectMock
    SetlPartySupplier setlPartySupplier;

    @BeforeEach
    public void beforeEach() {
        when(setlPartySupplier.getSetlPartyByDamlParty(anyString())).thenReturn(SetlParty.builder().name("Name").build());
    }

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
            if (balance.getType().equals(BalanceType.PESSIMISTIC.name())) {
                MatcherAssert.assertThat(balance.getBalance().doubleValue(), Matchers.is(liquidAmount));
            } else if (balance.getType().equals(BalanceType.ACTUAL.name())) {
                MatcherAssert.assertThat(balance.getBalance().doubleValue(), Matchers.is(liquidAmount + 2 * lockedAmount));
            } else if (balance.getType().equals(BalanceType.HOT.name())) {
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
        var liquidAmount = BigDecimal.valueOf(100);
        var balanceId = BalanceTestUtil.populateBalance(liquidAmount.doubleValue(),
                BalanceTestUtil.IBAN1,
                BalanceTestUtil.ASSET_CODE1,
                SANDBOX,
                getCurrentBankPartyId(),
                Balance.TEMPLATE_ID);

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured
                .get(String.format("/api/addresses/%s/balance", BalanceTestUtil.IBAN1))
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<>() {
                });

        // THEN
        Assertions.assertEquals(3, balances.size());
        Util.assertBigDecimalEquals(balances.get(0).getBalance(), liquidAmount);
        Util.assertBigDecimalEquals(balances.get(1).getBalance(), liquidAmount);
        Util.assertBigDecimalEquals(balances.get(2).getBalance(), liquidAmount);

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
    void GIVEN_local_balance_on_ledger_WHEN_get_request_local_balance_endpoint_THEN_return_correct_balances() throws InvalidProtocolBufferException {
        var liquidAmount = BigDecimal.valueOf(100);
        var localBalance = BalanceTestUtil.populateBalance(liquidAmount.doubleValue(), BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX,
                getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured
                .get(String.format("/api/getLocalBalance?address=%s", BalanceTestUtil.IBAN1))
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<>() {
                });

        // THEN
        MatcherAssert.assertThat(balances, Matchers.hasSize(3));
        Util.assertBigDecimalEquals(balances.get(0).getBalance(), liquidAmount);
        Util.assertBigDecimalEquals(balances.get(1).getBalance(), liquidAmount);
        Util.assertBigDecimalEquals(balances.get(2).getBalance(), liquidAmount);

        LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), Balance.TEMPLATE_ID, localBalance.getValue());
    }

    // ==
    @Test
    void GIVEN_non_local_balance_on_ledger_WHEN_get_request_local_balance_endpoint_THEN_return_correct_balances() throws InvalidProtocolBufferException {
        double liquidAmount = 100.0;
        var nonLocalBalance = BalanceTestUtil.populateBalance(liquidAmount, BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX,
                getSchedulerPartyId(), Optional.of(getCurrentBankPartyId().getValue()), Balance.TEMPLATE_ID);

        // WHEN
        RestAssured
                .get(String.format("/api/getLocalBalance?address=%s", BalanceTestUtil.IBAN1))
                .then().assertThat()
                .statusCode(404);

        LedgerBaseTest.cleanupContract(getSchedulerPartyId(), Balance.TEMPLATE_ID, nonLocalBalance.getValue());
    }

    @Test
    void GIVEN_only_zero_liquid_balance_on_ledger_WHEN_get_request_delete_address_THEN_balance_deleted()
            throws InvalidProtocolBufferException, InterruptedException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        var liquidAmount = BigDecimal.ZERO;
        BalanceTestUtil.populateBalance(liquidAmount.doubleValue(), BalanceTestUtil.IBAN1, BalanceTestUtil.ASSET_CODE1, SANDBOX, getCurrentBankPartyId(), Balance.TEMPLATE_ID);

        // WHEN
        RestAssured.delete(String.format("/api/ledger/addresses/%s", BalanceTestUtil.IBAN1))
                .then()
                .assertThat()
                .statusCode(204);

        Thread.sleep(1000);

        RestAssured
                .get(String.format("/api/getLocalBalance?address=%s", BalanceTestUtil.IBAN1))
                .then().assertThat()
                .statusCode(404);
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

    @Test
    void GIVEN_wallet_not_on_ledger_WHEN_get_get_balances_endpoint_THEN_return_404() {
        RestAssured.get(String.format("/api/balance/%s", BalanceTestUtil.WALLET_2))
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    void GIVEN_wallet_on_ledger_WHEN_get_get_balances_endpoint_THEN_return_correct_balances()
            throws InvalidProtocolBufferException {
        // GIVEN 1 balance, 2 locked balances, 2 incoming balances
        final var liquidAmount = BigDecimal.valueOf(100);
        final var lockedAmount = BigDecimal.valueOf(200);
        final var incomingAmount = BigDecimal.valueOf(300);

        var contracts = new ArrayList<List<ContractId>>();
        for (String iban : List.of(BalanceTestUtil.IBAN1, BalanceTestUtil.IBAN2)) {
            var balanceId1 = BalanceTestUtil.populateBalance(
                    liquidAmount.doubleValue(),
                    iban,
                    BalanceTestUtil.ASSET_CODE1,
                    SANDBOX,
                    getCurrentBankPartyId(),
                    Balance.TEMPLATE_ID
            );
            var lockedId1 = BalanceTestUtil.populateBalance(
                    lockedAmount.doubleValue(),
                    iban,
                    BalanceTestUtil.ASSET_CODE1,
                    SANDBOX,
                    getCurrentBankPartyId(),
                    LockedBalance.TEMPLATE_ID
            );
            var incomingId1 = BalanceTestUtil.populateBalance(
                    incomingAmount.doubleValue(),
                    iban,
                    BalanceTestUtil.ASSET_CODE1,
                    SANDBOX,
                    getCurrentBankPartyId(),
                    IncomingBalance.TEMPLATE_ID
            );
            contracts.add(List.of(balanceId1, lockedId1, incomingId1));
        }

        // WHEN
        List<com.rln.gui.backend.model.Balance> balances = RestAssured
                .get(String.format("/api/balance/%s", BalanceTestUtil.WALLET_1))
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<>() {
                });


        for (var balance : balances) {
            if (balance.getType().equals(BalanceType.PESSIMISTIC.name())) {
                Util.assertBigDecimalEquals(liquidAmount, balance.getBalance());
            } else if (balance.getType().equals(BalanceType.ACTUAL.name())) {
                Util.assertBigDecimalEquals(liquidAmount.add(lockedAmount), balance.getBalance());
            } else if (balance.getType().equals(BalanceType.HOT.name())) {
                Util.assertBigDecimalEquals((liquidAmount.add(incomingAmount)), balance.getBalance());
            }
        }

        for (List<ContractId> c : contracts) {
            LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), Balance.TEMPLATE_ID, c.get(0).getValue());
            LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), LockedBalance.TEMPLATE_ID, c.get(1).getValue());
            LedgerBaseTest.cleanupContract(getCurrentBankPartyId(), IncomingBalance.TEMPLATE_ID, c.get(2).getValue());
        }
    }

    private Map<String, Object> createBalanceChangeRequest(String assetId, String assetName, double change) {
        Map<String, Object> transferProposalRequest = new HashMap<>();
        transferProposalRequest.put("assetId", assetId);
        transferProposalRequest.put("assetName", assetName);
        transferProposalRequest.put("change", change);
        return transferProposalRequest;
    }
}
