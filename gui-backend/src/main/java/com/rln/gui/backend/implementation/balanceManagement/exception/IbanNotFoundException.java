/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.exception;

public class IbanNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 12345674L;

    public IbanNotFoundException(String iban) {
        super(String.format("Iban %s not found.", iban));
    }
}
