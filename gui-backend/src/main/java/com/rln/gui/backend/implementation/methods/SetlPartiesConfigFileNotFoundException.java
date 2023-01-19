/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import java.nio.file.Path;

public class SetlPartiesConfigFileNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 123456737L;

    public SetlPartiesConfigFileNotFoundException(Path file) {
        super(String.format("Setl Parties config file not found: %s.", file));
    }
}
