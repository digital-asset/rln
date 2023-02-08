/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.incoming;

import com.rln.client.kafkaClient.message.EnrichedPacs008;
import com.rln.messageprocessing.MessageProcessor;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

public class EnrichedSettlementChainKafkaListener {
  MessageProcessor<EnrichedPacs008> processor;

  public EnrichedSettlementChainKafkaListener(MessageProcessor<EnrichedPacs008> processor) {
    this.processor = processor;
  }

  @Incoming("enriched-message-in")
  public Uni<Void> acceptEnrichedSettlementChainMessage(Message<String> message) {
    var payload = MessageExtractor.extractPayloadAs(message, EnrichedPacs008.class);
    payload.ifPresent(processor);
    return Uni.createFrom().voidItem();
  }
}
