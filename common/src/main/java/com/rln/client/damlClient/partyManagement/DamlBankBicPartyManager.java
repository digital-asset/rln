/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import java.util.Random;
import java.util.Set;

public class DamlBankBicPartyManager implements PartyManager {

    private final BicPartyIdMapper mapper;
    private final Random random;

    public DamlBankBicPartyManager(BicPartyIdMapper mapper) {
        this.mapper = mapper;
        this.random = new Random();
    }

    @Override
    public String getParty(String BIC) {
        Set<String> partyIdsForBank = mapper.getBicToPartyIds().get(BIC);
        if (partyIdsForBank == null) {
            throw new NoSuchBicException(BIC);
        }
        return partyIdsForBank.stream().skip(random.nextInt(partyIdsForBank.size())).findAny()
                .orElseThrow(() -> new NoSuchBicException(BIC));
    }

    @Override
    public String getBic(String partyId) {
        String bic = mapper.getPartyIdToBic().get(partyId);
        if (bic == null) {
            throw new NoSuchPartyException(partyId);
        }
        return bic;
    }

    @Override
    public boolean hasPartyId(String partyId) {
        return mapper.getPartyIdToBic().containsKey(partyId);
    }

    @Override
    public boolean hasBic(String BIC) {
        return mapper.getBicToPartyIds().containsKey(BIC);
    }
}
