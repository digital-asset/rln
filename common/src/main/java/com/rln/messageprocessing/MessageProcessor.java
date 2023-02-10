/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.messageprocessing;

import java.util.function.Consumer;

public abstract class MessageProcessor<M> implements Consumer<M> {
    @Override
    public void accept(M input) {
        updateCache(input);
        publish(input);
    }

    public void updateCache(M input) {
        //default to no cache update
    }

    protected void publish(M input) {
        //default to no kafka submission
    }
    // todo refactor logging

}
