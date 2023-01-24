/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import java.nio.file.Path;

public class InternalServerError extends RuntimeException{
    private static final long serialVersionUID = 123456737L;

    public InternalServerError(Exception cause) {
        super(cause);
    }
}
