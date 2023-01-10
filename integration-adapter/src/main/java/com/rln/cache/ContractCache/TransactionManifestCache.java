/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.ContractCache;

import com.rln.cache.base.RLNTwoWayBaseCache;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest.ContractId;

/**
 * enabled: Bank Mode / Assembler Mode
 * key: groupId
 * value: TransactionManifestContractId
 */
public class TransactionManifestCache extends RLNTwoWayBaseCache<String, ContractId> {
}
