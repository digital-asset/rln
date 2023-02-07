/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
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
  public void update(Predicate<TransferProposal> filter,
      Function<TransferProposal, TransferProposal> remap) {
    var affectedProposals = proposals
        .stream()
        .filter(filter)
        .collect(Collectors.toSet());
    var newProposals = affectedProposals
        .stream()
        .map(remap)
        .collect(Collectors.toList());
    proposals.removeAll(affectedProposals);
    proposals.addAll(newProposals);
  }

  @Override
  public void save(TransferProposal proposal) {
    proposals.add(proposal);
  }

  @Override
  public Collection<TransferProposal> findAll(Predicate<TransferProposal> filter, Long limit, Long offset) {
    var elements = proposals.stream()
      .filter(Objects.requireNonNullElse(filter, t -> true))
      .skip(Objects.requireNonNullElse(offset, 0L));
    if (limit != null) {
      elements = elements.limit(limit);
    }
    return elements.collect(Collectors.toList());
  }

  @Override
  public void deleteAllByContractId(Collection<String> contractIds) {
    var idsToDelete = new HashSet<>(contractIds);
    proposals.removeIf(x -> idsToDelete.contains(x.getContractId()));
  }
}
