/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import com.daml.ledger.javaapi.data.codegen.ContractId;

public class UncachedContractIdException extends RuntimeException {
    private static final long serialVersionUID = 12345672L;

    public UncachedContractIdException(ContractId<?> contractId) {
        super("ContractId " + contractId.contractId + " was not cached, groupId unknown.");
    }
}
