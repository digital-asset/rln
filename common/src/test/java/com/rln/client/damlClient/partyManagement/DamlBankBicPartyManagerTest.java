/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

class DamlBankBicPartyManagerTest {
    private static final BicPartyIdMapper mapper = Mockito.mock(BicPartyIdMapper.class);
    private static final PartyManager partyManager = new DamlBankBicPartyManager(mapper);

    @Test
    void GIVEN_mapper_and_existing_bic_WHEN_get_party_THEN_return_correct_party() throws NoSuchBicException {
        String bic = "bic";
        String partyId1 = "partyId1";
        String partyId2 = "partyId2";
        Set<String> partyIds = Set.of(partyId1, partyId2);
        Mockito.when(mapper.getBicToPartyIds()).thenReturn(Map.of(bic, partyIds));

        String partyId = partyManager.getParty(bic);

        MatcherAssert.assertThat(partyIds.contains(partyId), Matchers.is(true));
    }

    @Test
    void GIVEN_mapper_and_non_existing_bic_WHEN_get_party_THEN_no_such_bic_exception_thrown() {
        String nonExistingBic = "someBic";
        Mockito.when(mapper.getBicToPartyIds()).thenReturn(Collections.emptyMap());

        Assertions.assertThrows(NoSuchBicException.class, () -> partyManager.getParty(nonExistingBic));
    }

    @Test
    void GIVEN_mapper_and_existing_party_WHEN_get_bic_THEN_return_correct_bic() {
        String partyId = "partyId";
        String bic = "bic";
        Mockito.when(mapper.getPartyIdToBic()).thenReturn(Map.of(partyId, bic));

        String mappedBic = partyManager.getBic(partyId);

        MatcherAssert.assertThat(mappedBic, Matchers.is(bic));
    }

    @Test
    void GIVEN_mapper_and_non_existing_party_WHEN_get_bic_THEN_no_such_party_exception_thrown() {
        String nonExistingParty = "someParty";
        Mockito.when(mapper.getPartyIdToBic()).thenReturn(Collections.emptyMap());

        Assertions.assertThrows(NoSuchPartyException.class, () -> partyManager.getBic(nonExistingParty));
    }
}
