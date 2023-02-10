/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.daml;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.cache.ContractCache.InitiateTransferCache;
import com.rln.cache.ContractCache.InitiateTransferCache.InitiatorAndContractId;
import com.rln.cache.PartyCache.SchedulerPartyCache;
import com.rln.client.kafkaClient.message.InitiateTransfer;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.conversion.daml2kafka.InitiateTransferContractCreationToKafka;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.ContractId;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DamlInitiationContractCreationProcessor extends MessageProcessor<CreatedEvent> {

  private static final Logger logger = LoggerFactory.getLogger(DamlInitiationContractCreationProcessor.class);

  private final KafkaSubmitter<InitiateTransfer> kafkaSubmitter;
  private final InitiateTransferContractCreationToKafka conversion;
  private final InitiateTransferCache initiateTransferCache;
  private final SchedulerPartyCache schedulerPartyCache;


  public DamlInitiationContractCreationProcessor(KafkaSubmitter<InitiateTransfer> kafkaSubmitter,
                                                 InitiateTransferContractCreationToKafka conversion,
                                                 InitiateTransferCache initiateTransferCache,
                                                 SchedulerPartyCache schedulerPartyCache) {
    logger.info("Created DamlInitiationContractCreationProcessor with translation {}", conversion);
    this.kafkaSubmitter = kafkaSubmitter;
    this.conversion = conversion;
    this.initiateTransferCache = initiateTransferCache;
    this.schedulerPartyCache = schedulerPartyCache;
  }

  @Override
  public void updateCache(CreatedEvent input) {
    var initiateTransfer = com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer.fromValue(input.getArguments());
    var contractId = new ContractId(input.getContractId());
    logger.info("Updating cache with Daml InitiateTransfer contract {}", initiateTransfer);

    schedulerPartyCache.write(initiateTransfer.groupId, initiateTransfer.scheduler);
    InitiatorAndContractId initiatorAndContractId = new InitiatorAndContractId(initiateTransfer.initiator, contractId);
    initiateTransferCache.write(initiateTransfer.groupId, initiatorAndContractId);
  }

  @Override
  protected void publish(CreatedEvent input) {
    var converted = conversion.createdEventToKafka(input);
    logger.info("Publishing to Kafka, GroupId: {}", converted.getGroupId());
    kafkaSubmitter.submit(converted);
    logger.debug("Published to Kafka, GroupId: {}, Message: {}", converted.getGroupId(), converted);
  }
}
