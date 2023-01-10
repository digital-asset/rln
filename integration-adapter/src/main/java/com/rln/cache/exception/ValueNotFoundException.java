/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.exception;

public class ValueNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 12345676L;

    public ValueNotFoundException(String value) {
        super("Cannot find value " + value + " in cache");
    }
}
