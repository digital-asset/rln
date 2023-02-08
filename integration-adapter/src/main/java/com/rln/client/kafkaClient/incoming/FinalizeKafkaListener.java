/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.incoming;

import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.client.kafkaClient.message.InitiateTransfer;
import com.rln.messageprocessing.MessageProcessor;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

public class FinalizeKafkaListener {
  MessageProcessor<FinalizeRejectSettlement>  processor;

  public FinalizeKafkaListener(MessageProcessor<FinalizeRejectSettlement> processor) {
    this.processor = processor;
  }

  @Incoming("finalize-reject-settlement-message-in")
  public Uni<Void> acceptFinalizeMessage(Message<String> message) {
    var payload = MessageExtractor.extractPayloadAs(message, FinalizeRejectSettlement.class);
    payload.ifPresent(processor);
    return Uni.createFrom().voidItem();
  }
}
