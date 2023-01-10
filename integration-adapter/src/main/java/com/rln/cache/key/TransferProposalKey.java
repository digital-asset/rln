/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.cache.key;

import lombok.Data;

@Data
public class TransferProposalKey {
    private final String groupId;
    private final String messageId;
    private final String bankBic;
}
