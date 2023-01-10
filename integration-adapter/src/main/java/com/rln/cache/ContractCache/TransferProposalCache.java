/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.ContractCache;

import com.rln.cache.base.RLNTwoWayBaseCache;
import com.rln.cache.key.TransferProposalKey;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;

/**
 * enabled: Bank Mode / Assembler Mode
 * key: groupId, messageId
 * value: TransferProposalContractId
 */
public class TransferProposalCache extends RLNTwoWayBaseCache<TransferProposalKey, TransferProposal.ContractId> {

}
