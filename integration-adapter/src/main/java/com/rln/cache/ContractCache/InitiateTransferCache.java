/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.ContractCache;

import com.rln.cache.base.RLNBaseCache;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import lombok.Data;

/**
 * enabled: scheduler mode
 * key: groupId
 * value: initiatorPartyId, InitiateTransferContractId
 */
public class InitiateTransferCache extends RLNBaseCache<String, InitiateTransferCache.InitiatorAndContractId> {
    @Data
    public static class InitiatorAndContractId {
        public final String initiatorPartyId;
        public final InitiateTransfer.ContractId contractId;
    }
}
