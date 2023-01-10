/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.exception;

import com.daml.ledger.javaapi.data.ContractId;

public class ContractIdNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 12345673L;

    public ContractIdNotFoundException(ContractId cid) {
        super(String.format("%s not found.", cid));
    }
}
