/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.kafka2daml;

import com.rln.cache.ContractCache.TransferProposalCache;
import com.rln.cache.PartyCache.BankPartyCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.client.damlClient.ApproveRejectProposalChoiceParameters;
import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.client.kafkaClient.message.fields.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class ApproveRejectProposalToDamlTranslation implements Function<ApproveRejectProposal, ApproveRejectProposalChoiceParameters> {

    private static final Logger logger = LoggerFactory.getLogger(ApproveRejectProposalToDamlTranslation.class);
    // Integration Adapter always settles off-ledger as it connects non-Daml participants to the network.
    private static final boolean IA_DOES_NOT_SETTLE_ON_LEDGER = false;
    private static final boolean NOT_YET_APPROVED = false;

    private final BankPartyCache bankPartyCache;
    private final TransferProposalCache transferProposalCache;

    public ApproveRejectProposalToDamlTranslation(BankPartyCache bankPartyCache, TransferProposalCache transferProposalCache) {
        this.bankPartyCache = bankPartyCache;
        this.transferProposalCache = transferProposalCache;
    }

    @Override
    public ApproveRejectProposalChoiceParameters apply(ApproveRejectProposal approveRejectProposal) {
        TransferProposalKey transferProposalKey = new TransferProposalKey(approveRejectProposal.getGroupId(),
                approveRejectProposal.getMessageId(), approveRejectProposal.getBankBic());
        var bankPartyId = bankPartyCache.read(transferProposalKey);
        var contractId = transferProposalCache.readFromKeyToValue(transferProposalKey);
        var isApproveOperation = Status.APPROVE.equals(approveRejectProposal.getStatus());
        logger.info("Translation result bankPartyId {}, contractId {}, approve operation {}", bankPartyId, contractId, isApproveOperation);
        return new ApproveRejectProposalChoiceParameters(
                bankPartyId, contractId.contractId, NOT_YET_APPROVED, isApproveOperation,
                approveRejectProposal.getReason(), IA_DOES_NOT_SETTLE_ON_LEDGER);
    }
}
