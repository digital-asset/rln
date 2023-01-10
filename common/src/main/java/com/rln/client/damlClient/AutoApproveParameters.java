/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.CreateCommand;
import com.daml.ledger.javaapi.data.Unit;
import com.rln.damlCodegen.da.internal.template.Archive;
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveTransferProposalMarker;
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveType;
import com.rln.damlCodegen.workflow.transferproposal.autoapprovetype.FullAuto;
import com.rln.damlCodegen.workflow.transferproposal.autoapprovetype.LimitedMaxAmount;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@AllArgsConstructor
@Data
public class AutoApproveParameters implements Parameters {
  public static enum ApprovalMode {
    AUTO, LIMIT, MANUAL
  }
  private Instant currentTime;
  private String owner;
  private String label;
  private AutoApproveTransferProposalMarker.ContractId contractId;
  private ApprovalMode approvalMode;
  private BigDecimal maxAmount;

  public Optional<Command> createOrUpdate() {
    return isCreate() ? createCommand() : updateCommand();
  }

  public String event() {
    var mode = isCreate() ? "Creating" : "Updating";
    return String.format("%s AutoApprove Marker (%s, %s, %s)", mode, owner, label, approvalMode);
  }

  private Optional<Command> updateCommand() {
    switch (approvalMode) {
      case AUTO:
        return Optional.of(contractId.exerciseUpdateMarker(currentTime, new FullAuto(Unit.getInstance())));
      case LIMIT:
        return Optional.of(contractId.exerciseUpdateMarker(currentTime, new LimitedMaxAmount(maxAmount)));
      default:
        return Optional.of(contractId.exerciseArchive(new Archive()));
    }
  }

  private Optional<Command> createCommand() {
    switch (approvalMode) {
      case AUTO:
        return Optional.of(new AutoApproveTransferProposalMarker(owner, currentTime, label, new FullAuto(Unit.getInstance())).create());
      case LIMIT:
        return Optional.of(new AutoApproveTransferProposalMarker(owner, currentTime, label, new LimitedMaxAmount(maxAmount)).create());
      default:
        return Optional.empty();
    }
  }

  private boolean isCreate() {
    return Objects.isNull(contractId);
  }
}
