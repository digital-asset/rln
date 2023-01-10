/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.kafka2daml;

import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.cache.ContractCache.InitiateTransferCache.InitiatorAndContractId;
import com.rln.cache.PartyCache.SchedulerPartyCache;
import com.rln.client.damlClient.CreateProposalsChoiceParameters;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.client.kafkaClient.message.EnrichedPacs008;
import com.rln.client.kafkaClient.message.fields.MessageIdWithStepsAndPayload;
import com.rln.client.kafkaClient.message.fields.Step;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Instrument;
import com.rln.damlCodegen.workflow.data.Leg;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class EnrichedPacs008SwiftToDamlTranslation implements Function<EnrichedPacs008, CreateProposalsChoiceParameters> {
    private static final Logger logger = LoggerFactory.getLogger(EnrichedPacs008SwiftToDamlTranslation.class);
    private final PartyManager partyManager;
    private final RandomShardPartyPicker randomShardPartyPicker;
    private final SchedulerPartyCache schedulerPartyCache;
    private final InitiateTransferCache initiateTransferContractIdCache;

    public EnrichedPacs008SwiftToDamlTranslation(PartyManager partyManager, RandomShardPartyPicker randomShardPartyPicker, SchedulerPartyCache schedulerPartyCache, InitiateTransferCache initiateTransferContractIdCache) {
        this.partyManager = partyManager;
        this.randomShardPartyPicker = randomShardPartyPicker;
        this.schedulerPartyCache = schedulerPartyCache;
        this.initiateTransferContractIdCache = initiateTransferContractIdCache;
    }

    @Override
    public CreateProposalsChoiceParameters apply(EnrichedPacs008 enrichedPacs008) {
        var assemblerPartyId = randomShardPartyPicker.pickRandomShardParty();
        String schedulerPartyId = schedulerPartyCache.read(enrichedPacs008.getGroupId());
        InitiatorAndContractId initiateTransfer = initiateTransferContractIdCache.read(enrichedPacs008.getGroupId());
        var messageIdToLegs = convertToMessageIdToLegs(initiateTransfer.getInitiatorPartyId(), enrichedPacs008.getMessages());
        logger.info("Translated result: GroupId {}, SchedulerPartyId {}, AssemblerPartyId {}", enrichedPacs008.getGroupId(), schedulerPartyId, assemblerPartyId);
        logger.debug("Translated result: GroupId {}, MessageIdToLegs {}", enrichedPacs008.getGroupId(), messageIdToLegs);
        return new CreateProposalsChoiceParameters(schedulerPartyId, assemblerPartyId, messageIdToLegs, initiateTransfer.getContractId());
    }

    private List<Tuple2<String, Leg>> convertToMessageIdToLegs(String initiatorPartyId, MessageIdWithStepsAndPayload[] messageIdWithBicsAndPayloads) {
        String initiatorBic = partyManager.getBic(initiatorPartyId);
        var result = new ArrayList<Tuple2<String, Leg>>(messageIdWithBicsAndPayloads.length);
        for (MessageIdWithStepsAndPayload actualItem : messageIdWithBicsAndPayloads) {
            var translatedParties = translateBics(initiatorPartyId, initiatorBic, actualItem.getSteps());
            var leg = new Leg(actualItem.getPayload(), translatedParties);
            result.add(new Tuple2<String, Leg>(actualItem.getMessageId(), leg));
        }
        return result;
    }

    private List<Tuple2<String, SettlementStep>> translateBics(String initiatorPartyId, String initiatorBic, Step[] steps) {
        var result = new ArrayList<Tuple2<String, SettlementStep>>(steps.length);
        for (Step step : steps) {
            if (step.getApprover().equals(initiatorBic)) {
                result.add(new Tuple2<>(initiatorPartyId, toSettlementStep(step)));
            } else {
                result.add(new Tuple2<>(partyManager.getParty(step.getApprover()), toSettlementStep(step)));
            }
        }
        return result;
    }

    private SettlementStep toSettlementStep(Step step) {
        Instrument delivery = new Instrument(BigDecimal.valueOf(step.getAmount()), step.getLabel());
        return new SettlementStep(Optional.ofNullable(step.getSender()), Optional.ofNullable(step.getReceiver()), delivery);
    }
}
