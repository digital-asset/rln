/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.FiltersByParty;
import com.daml.ledger.javaapi.data.InclusiveFilter;
import com.rln.client.damlClient.RLNClient;
import com.rln.damlCodegen.model.bankbic.BankBIC;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BicPartyIdMapper {

    private static final Logger logger = LoggerFactory.getLogger(BicPartyIdMapper.class);
    // same bic can be mapped to multiple partyIds as we will shard the banks
    private final Map<String, Set<String>> bicToPartyIds = new HashMap<>();
    private final Map<String, String> partyIdToBic = new HashMap<>();


    public BicPartyIdMapper(RLNClient rlnClient, String bankBicReadingParty) {
        // In Bank mode: we are only expecting to get one contract that contains bic to all bank party shards
        // In Scheduler mode: we are expecting to see many bankBic contracts from different banks
        logger.info("Reading BankBicMapping with bankBicReadingParty {}", bankBicReadingParty);
        Flowable<CreatedEvent> activeContracts = rlnClient.getActiveContracts(new FiltersByParty(Collections.singletonMap(bankBicReadingParty,
                new InclusiveFilter(Set.of(BankBIC.TEMPLATE_ID)))));

        activeContracts.map(e -> BankBIC.fromValue(e.getArguments()))
                .forEach(bankBic -> {
                    logger.info("BankBic read from ledger {}", bankBic);
                    String bic = bankBic.bic;
                    Set<String> bankShardPartyIds = bankBic.banks.map.keySet();
                    bicToPartyIds.put(bic, bankShardPartyIds);
                    bankShardPartyIds.forEach(bankPartyId -> partyIdToBic.put(bankPartyId, bic));
                });
    }

    public Map<String, Set<String>> getBicToPartyIds() {
        return bicToPartyIds;
    }

    public Map<String, String> getPartyIdToBic() {
        return partyIdToBic;
    }
}
