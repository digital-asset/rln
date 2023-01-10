/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.subscription;

import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.TransactionTree;
import io.reactivex.functions.Consumer;

public interface LedgerSubscriber<R> {
     R subscribe (String subscribeParty, Identifier templateId, Consumer<TransactionTree> consumer);
}
