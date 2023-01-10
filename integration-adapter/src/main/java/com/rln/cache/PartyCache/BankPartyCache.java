/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.PartyCache;

import com.rln.cache.base.RLNBaseCache;
import com.rln.cache.key.TransferProposalKey;

/**
 * enabled: Bank Mode
 * key: groupId, messageId
 * value: bankPartyId
 */
public class BankPartyCache extends RLNBaseCache<TransferProposalKey, String> {
}
