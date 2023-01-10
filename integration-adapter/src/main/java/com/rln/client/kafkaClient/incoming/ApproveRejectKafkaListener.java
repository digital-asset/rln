/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.incoming;

import com.rln.client.kafkaClient.message.ApproveRejectProposal;
import com.rln.messageprocessing.MessageProcessor;
import org.eclipse.microprofile.reactive.messaging.Incoming;

public class ApproveRejectKafkaListener {
  MessageProcessor<ApproveRejectProposal> processor;

  public ApproveRejectKafkaListener(MessageProcessor<ApproveRejectProposal> processor) {
    this.processor = processor;
  }

  @Incoming("approve-reject-message-in")
  public void acceptRejectMessage(ApproveRejectProposal message) {
    processor.accept(message);
  }
}
