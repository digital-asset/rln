/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.kafka2daml;

import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.cache.PartyCache.AssemblerPartyCache;
import com.rln.client.damlClient.FinalizeRejectSettlementChoiceParameters;
import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.client.kafkaClient.message.fields.Status;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.ContractId;

import java.util.function.Function;

public class FinalizeRejectSettlementToDamlTranslation implements Function<FinalizeRejectSettlement, FinalizeRejectSettlementChoiceParameters> {

    private final AssemblerPartyCache assemblerPartyCache;
    private final TransactionManifestCache transactionManifestCache;

    public FinalizeRejectSettlementToDamlTranslation(AssemblerPartyCache assemblerPartyCache, TransactionManifestCache transactionManifestCache) {
        this.assemblerPartyCache = assemblerPartyCache;
        this.transactionManifestCache = transactionManifestCache;
    }

    @Override
    public FinalizeRejectSettlementChoiceParameters apply(FinalizeRejectSettlement finalizeRejectSettlement) {
        String assemblerPartyId = assemblerPartyCache.read(finalizeRejectSettlement.getGroupId());
        ContractId contractId = transactionManifestCache.readFromKeyToValue(finalizeRejectSettlement.getGroupId());
        var approved = Status.APPROVE.equals(finalizeRejectSettlement.getStatus());
        return new FinalizeRejectSettlementChoiceParameters(assemblerPartyId, approved, finalizeRejectSettlement.getReason(), contractId);
    }
}
