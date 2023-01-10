/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.conversion.kafka2daml;

import com.rln.CommonBaseTest;
import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.cache.ContractCache.InitiateTransferCache.InitiatorAndContractId;
import com.rln.cache.PartyCache.SchedulerPartyCache;
import com.rln.client.damlClient.CreateProposalsChoiceParameters;
import com.rln.client.damlClient.partyManagement.NoSuchBicException;
import com.rln.client.damlClient.partyManagement.NoSuchPartyException;
import com.rln.client.damlClient.partyManagement.PartyManager;
import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.client.kafkaClient.message.EnrichedPacs008;
import com.rln.client.kafkaClient.message.fields.MessageIdWithStepsAndPayload;
import com.rln.client.kafkaClient.message.fields.Step;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.Leg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

class EnrichedPacs008SwiftToDamlTranslationTest extends CommonBaseTest {
    InitiatorAndContractId initiatorAndContractId = new InitiatorAndContractId(INITIATOR_PARTY_ID, INITIATE_TRANSFER_CONTRACT_ID);
    PartyManager partyManager = Mockito.mock(PartyManager.class);
    RandomShardPartyPicker randomShardPartyPicker = Mockito.mock(RandomShardPartyPicker.class);
    SchedulerPartyCache schedulerPartyCache = Mockito.mock(SchedulerPartyCache.class);
    InitiateTransferCache initiateTransferContractIdCache = Mockito.mock(InitiateTransferCache.class);

    EnrichedPacs008SwiftToDamlTranslation translation =
            new EnrichedPacs008SwiftToDamlTranslation(
                    partyManager, randomShardPartyPicker, schedulerPartyCache, initiateTransferContractIdCache);

    @BeforeEach
    public void setup() throws NoSuchPartyException, NoSuchBicException {
        Mockito.when((partyManager.getBic(INITIATOR_PARTY_ID))).thenReturn(BANK11_BIC);
        Mockito.when((partyManager.getParty(BANK22_BIC))).thenReturn(AGENT_BANK_PARTY_ID);
        Mockito.when((partyManager.getParty(BANK12_BIC))).thenReturn(AGENT_BANK_PARTY_ID2);
        Mockito.when(randomShardPartyPicker.pickRandomShardParty()).thenReturn(ASSEMBLER_PARTY_ID);
        Mockito.when(schedulerPartyCache.read(ArgumentMatchers.anyString())).thenReturn(SCHEDULER_PARTY_ID);
        Mockito.when(initiateTransferContractIdCache.read(ArgumentMatchers.anyString())).thenReturn(initiatorAndContractId);
    }

    @Test
    void worksWithEmptyMessages() {
        MessageIdWithStepsAndPayload[] messages = new MessageIdWithStepsAndPayload[]{};
        EnrichedPacs008 enrichedPacs008 = new EnrichedPacs008(GROUP_ID, messages);

        var parameters = translation.apply(enrichedPacs008);

        Assertions.assertEquals(
                new CreateProposalsChoiceParameters(SCHEDULER_PARTY_ID, ASSEMBLER_PARTY_ID, Collections.emptyList(), INITIATE_TRANSFER_CONTRACT_ID),
                parameters);
    }

    @Test
    void worksWithNormalInput() {
        MessageIdWithStepsAndPayload item = new MessageIdWithStepsAndPayload(MESSAGE_ID, new Step[]{new Step(BANK11_BIC, SENDER_IBAN, RECEIVER_IBAN, 10.0, USD)}, PAYLOAD);
        MessageIdWithStepsAndPayload[] messages = new MessageIdWithStepsAndPayload[]{item};
        EnrichedPacs008 enrichedPacs008 = new EnrichedPacs008(GROUP_ID, messages);

        var parameters = translation.apply(enrichedPacs008);

        Assertions.assertEquals(SCHEDULER_PARTY_ID, parameters.getSchedulerPartyId());
        Assertions.assertEquals(ASSEMBLER_PARTY_ID, parameters.getAssemblerPartyId());
        Assertions.assertEquals(INITIATE_TRANSFER_CONTRACT_ID, parameters.getInitiateTransferCid());
        List<Tuple2<String, Leg>> legs = parameters.getMessageIdToLegs();
        Assertions.assertEquals(1, legs.size());
        // TODO
        // assertEquals(List.of(AGENT_BANK_PARTY_ID, INITIATOR_PARTY_ID, AGENT_BANK_PARTY_ID2), legs.get(0)._2.approvers);
    }

    @Test
    void throwsOnNoSuchParty() {
        MessageIdWithStepsAndPayload item = new MessageIdWithStepsAndPayload(MESSAGE_ID, new Step[]{new Step(BANK11_BIC, SENDER_IBAN, RECEIVER_IBAN, 10.0, USD)}, PAYLOAD);

        MessageIdWithStepsAndPayload[] messages = new MessageIdWithStepsAndPayload[]{item};
        EnrichedPacs008 enrichedPacs008 = new EnrichedPacs008(GROUP_ID, messages);
        Mockito.when((partyManager.getBic(ArgumentMatchers.anyString()))).thenThrow(new NoSuchPartyException(AGENT_BANK_PARTY_ID));

        Assertions.assertThrows(NoSuchPartyException.class, () -> translation.apply(enrichedPacs008));
    }

    @Test
    void throwsOnNoSuchBic() {
        MessageIdWithStepsAndPayload item = new MessageIdWithStepsAndPayload(MESSAGE_ID, new Step[]{new Step(BANK22_BIC, SENDER_IBAN, RECEIVER_IBAN, 10.0, USD)}, PAYLOAD);
        MessageIdWithStepsAndPayload[] messages = new MessageIdWithStepsAndPayload[]{item};
        EnrichedPacs008 enrichedPacs008 = new EnrichedPacs008(GROUP_ID, messages);
        Mockito.when((partyManager.getParty(ArgumentMatchers.anyString()))).thenThrow(new NoSuchBicException(BANK22_BIC));

        Assertions.assertThrows(NoSuchBicException.class, () -> translation.apply(enrichedPacs008));
    }
}
