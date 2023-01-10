/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.listeners.base;

import com.daml.ledger.javaapi.data.ExercisedEvent;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.TreeEvent;
import com.rln.client.damlClient.subscription.RLNLedgerSubscriber;
import com.rln.messageprocessing.MessageProcessor;

import java.util.List;
import java.util.function.Predicate;

public abstract class ExercisedEventDamlListener extends EventDamlListener<ExercisedEvent> {
    public ExercisedEventDamlListener(List<String> shardPartyIds, RLNLedgerSubscriber subscriber, MessageProcessor<ExercisedEvent> messageProcessor,
                                      Identifier templateId, List<String> choiceNames) {
        super(shardPartyIds, subscriber, messageProcessor, templateId, ExercisedEvent.class, hasChoiceName(choiceNames));
    }

    private static Predicate<TreeEvent> hasChoiceName(List<String> choiceNames) {
        return treeEvent -> {
            var protoTreeEvent = treeEvent.toProtoTreeEvent();
            return protoTreeEvent.hasExercised() &&
                choiceNames.stream().anyMatch(
                    choiceName -> protoTreeEvent.getExercised().getChoice().equals(choiceName));
        };
    }
}
