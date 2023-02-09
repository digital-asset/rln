/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.incoming;

import com.rln.client.kafkaClient.message.EnrichedPacs008;
import com.rln.messageprocessing.MessageProcessor;
import org.eclipse.microprofile.reactive.messaging.Incoming;

public class EnrichedSettlementChainKafkaListener {
  MessageProcessor<EnrichedPacs008> processor;

  public EnrichedSettlementChainKafkaListener(MessageProcessor<EnrichedPacs008> processor) {
    this.processor = processor;
  }

  @Incoming("enriched-message-in")
  public void acceptEnrichedSettlementChainMessage(String message) {
    var payload = MessageExtractor.extractAs(message, EnrichedPacs008.class);
    payload.ifPresent(processor);
  }
}
