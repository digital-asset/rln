/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.exercise;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.rln.client.damlClient.listeners.base.ExercisedEventDamlListener;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.common.Constants;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.messageprocessing.MessageProcessor;

import java.util.List;

public class FinalizeRejectSettlementDamlListener extends ExercisedEventDamlListener {
    public FinalizeRejectSettlementDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber, MessageProcessor<ExercisedEvent> messageProcessor) {
        super(shardPartyIds, subscriber, messageProcessor, TransactionManifest.TEMPLATE_ID, List.of(Constants.FINALIZE_SETTLEMENT_CHOICE, Constants.REJECT_SETTLEMENT_CHOICE));
    }
}
