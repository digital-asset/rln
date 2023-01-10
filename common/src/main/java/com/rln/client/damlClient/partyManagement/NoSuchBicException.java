/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

public class NoSuchBicException extends RuntimeException {
    private static final long serialVersionUID = 12345670L;

    public NoSuchBicException(String BIC) {
        super("BIC: " + BIC + " does not have any corresponding party");
    }
}
