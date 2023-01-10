/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods;

import java.util.Collection;

public interface TransferProposalRepository {
  void save(TransferProposal proposal);

  Collection<TransferProposal> findAllWaiting();

  Collection<TransferProposal> findAll(Long limit, Long offset);

  void deleteAllByContractId(Collection<String> contractIds);
}
