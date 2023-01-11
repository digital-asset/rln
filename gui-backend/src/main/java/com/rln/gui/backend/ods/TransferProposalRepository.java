/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods;

import java.util.Collection;
import java.util.function.Predicate;

public interface TransferProposalRepository {
  void save(TransferProposal proposal);

  default Collection<TransferProposal> findAllWaiting() {
    return findAll(TransferProposal::isWaiting, null, null);
  }

  Collection<TransferProposal> findAll(Predicate<TransferProposal> filter, Long limit, Long offset);

  void deleteAllByContractId(Collection<String> contractIds);
}
