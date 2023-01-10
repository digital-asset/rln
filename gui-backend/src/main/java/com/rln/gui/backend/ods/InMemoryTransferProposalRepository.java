/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@ApplicationScoped
@Named("in-memory")
public class InMemoryTransferProposalRepository implements TransferProposalRepository {
  private final Collection<TransferProposal> proposals = new ConcurrentLinkedQueue<>();

  @Override
  public void save(TransferProposal proposal) {
    proposals.add(proposal);
  }

  @Override
  public Collection<TransferProposal> findAllWaiting() {
    return proposals.stream()
      .filter(TransferProposal::isWaiting)
      .collect(Collectors.toList());
  }

  @Override
  public Collection<TransferProposal> findAll(Long limit, Long offset) {
    return proposals.stream()
      .skip(offset)
      .limit(limit)
      .collect(Collectors.toList());
  }

  @Override
  public void deleteAllByContractId(Collection<String> contractIds) {
    var idsToDelete = new HashSet<>(contractIds);
    proposals.removeIf(x -> idsToDelete.contains(x.getContractId()));
  }
}
