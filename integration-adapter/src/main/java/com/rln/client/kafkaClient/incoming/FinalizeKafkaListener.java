/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.incoming;

import com.rln.client.kafkaClient.message.FinalizeRejectSettlement;
import com.rln.messageprocessing.MessageProcessor;
import org.eclipse.microprofile.reactive.messaging.Incoming;

public class FinalizeKafkaListener {
  MessageProcessor<FinalizeRejectSettlement>  processor;

  public FinalizeKafkaListener(MessageProcessor<FinalizeRejectSettlement> processor) {
    this.processor = processor;
  }

  @Incoming("finalize-reject-settlement-message-in")
  public void acceptFinalizeMessage(FinalizeRejectSettlement message) {
    processor.accept(message);
  }
}
