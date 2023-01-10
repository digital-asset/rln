/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.outgoing;

import org.eclipse.microprofile.reactive.messaging.Emitter;


public class KafkaSubmitter<T> {
    private final Emitter<T> emitter;

    public KafkaSubmitter(Emitter<T> emitter) {
        this.emitter = emitter;
    }

    public void submit(T message) {
        emitter.send(message);
    }
}
