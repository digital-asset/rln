/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.exception;

public class KeyNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 12345675L;

    public KeyNotFoundException(String key) {
        super("Cannot find key " + key + " in cache");
    }
}
