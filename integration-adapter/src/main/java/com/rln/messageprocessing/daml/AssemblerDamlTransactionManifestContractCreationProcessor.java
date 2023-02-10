/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing.daml;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.rln.cache.ContractCache.TransactionManifestCache;
import com.rln.cache.PartyCache.AssemblerPartyCache;
import com.rln.client.kafkaClient.message.TransactionManifest;
import com.rln.client.kafkaClient.outgoing.KafkaSubmitter;
import com.rln.conversion.daml2kafka.CreatedEventConverter;
import com.rln.conversion.daml2kafka.TransactionManifestContractCreationToKafka;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.ContractId;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AssemblerDamlTransactionManifestContractCreationProcessor extends MessageProcessor<CreatedEvent> {

  private static final Logger logger = LoggerFactory.getLogger(AssemblerDamlTransactionManifestContractCreationProcessor.class);
  private final KafkaSubmitter<TransactionManifest> kafkaSubmitter;
  private final CreatedEventConverter<TransactionManifest> conversion;
  private final TransactionManifestCache transactionManifestCache;
  private final AssemblerPartyCache assemblerPartyCache;

  public AssemblerDamlTransactionManifestContractCreationProcessor(KafkaSubmitter<TransactionManifest> kafkaSubmitter,
                                                                   TransactionManifestContractCreationToKafka conversion,
                                                                   TransactionManifestCache transactionManifestCache,
                                                                   AssemblerPartyCache assemblerPartyCache) {
    logger.info("Created AssemblerDamlTransactionManifestContractCreationProcessor with translation {}", conversion);
    this.kafkaSubmitter = kafkaSubmitter;
    this.conversion = conversion;
    this.transactionManifestCache = transactionManifestCache;
    this.assemblerPartyCache = assemblerPartyCache;
  }

  @Override
  public void updateCache(CreatedEvent input) {
    var transactionManifest = com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.fromValue(input.getArguments());
    var contractId = new ContractId(input.getContractId());
    logger.info("Updating cache (Assembler party Cache, Transaction Manifest Cache) with {}", transactionManifest.groupId);
    assemblerPartyCache.write(transactionManifest.groupId, transactionManifest.assembler);
    transactionManifestCache.write(transactionManifest.groupId, contractId);
  }

  @Override
  protected void publish(CreatedEvent input) {
    var converted = conversion.createdEventToKafka(input);
    logger.info("Publishing to Kafka, GroupId: {}", converted.getGroupId());
    kafkaSubmitter.submit(converted);
    logger.debug("Published to Kafka, GroupId: {}, Message: {}", converted.getGroupId(), converted);
  }
}
