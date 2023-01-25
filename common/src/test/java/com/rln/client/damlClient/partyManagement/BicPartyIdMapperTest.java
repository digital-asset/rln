/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;


import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Unit;
import com.rln.client.damlClient.RLNClient;
import com.rln.damlCodegen.da.set.types.Set;
import com.rln.damlCodegen.model.bankbic.BankBIC;
import io.reactivex.Flowable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

class BicPartyIdMapperTest {
    private static final Set<String> EMPTY_SET = new Set<>(Collections.emptyMap());

    private static final String BANK_BIC_READING_PARTY_ID = "someBankPartyShard";
    private static final String BANK_A1_PARTY_ID = "bankA1";
    private static final String BANK_A_BIC = "bankABic";
    private static final String BANK_A2_PARTY_ID = "bankA2";
    private static final String BANK_B1_PARTY_ID = "bankB1";
    private static final String BANK_B_BIC = "bankBBic";

    @Test
    void GIVEN_active_contracts_WHEN_mapper_instantiated_THEN_mapping_created_correctly() {
        RLNClient rlnClient = Mockito.mock(RLNClient.class);

        Set<String> bankAShardPartyIds = new Set<>(Map.of(BANK_A1_PARTY_ID, Unit.getInstance(), BANK_A2_PARTY_ID, Unit.getInstance()));
        Set<String> bankBShardPartyIds = new Set<>(Collections.singletonMap(BANK_B1_PARTY_ID, Unit.getInstance()));
        BankBIC bankA1 = new BankBIC(BANK_A_BIC, bankAShardPartyIds, EMPTY_SET, EMPTY_SET, EMPTY_SET);
        BankBIC bankB1 = new BankBIC(BANK_B_BIC, bankBShardPartyIds, EMPTY_SET, EMPTY_SET, EMPTY_SET);

        CreatedEvent event1 = Mockito.mock(CreatedEvent.class);
        CreatedEvent event2 = Mockito.mock(CreatedEvent.class);

        Mockito.when(event1.getArguments()).thenReturn(bankA1.toValue());
        Mockito.when(event2.getArguments()).thenReturn(bankB1.toValue());

        Mockito.when(rlnClient.getActiveContracts(ArgumentMatchers.any())).thenReturn(Flowable.just(event1, event2));

        BicPartyIdMapper mapper = new BicPartyIdMapper(rlnClient, BANK_BIC_READING_PARTY_ID);

        MatcherAssert.assertThat(mapper.getBicToPartyIds(), Matchers.is(Map.of(
                BANK_A_BIC, java.util.Set.of(BANK_A1_PARTY_ID, BANK_A2_PARTY_ID),
                BANK_B_BIC, java.util.Set.of(BANK_B1_PARTY_ID))));
        MatcherAssert.assertThat(mapper.getPartyIdToBic(), Matchers.is(Map.of(
                BANK_A1_PARTY_ID, BANK_A_BIC,
                BANK_A2_PARTY_ID, BANK_A_BIC,
                BANK_B1_PARTY_ID, BANK_B_BIC
        )));
    }
}
