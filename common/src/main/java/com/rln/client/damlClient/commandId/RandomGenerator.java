/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.commandId;

import java.util.UUID;

public class RandomGenerator implements Generator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
