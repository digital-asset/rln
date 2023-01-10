/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

public class NoSuchPartyException extends RuntimeException {
    private static final long serialVersionUID = 12345671L;

    public NoSuchPartyException(String partyId) {
        super("partyId: " + partyId + " does not have any corresponding bic");
    }
}
