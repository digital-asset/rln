/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.methods;

import com.daml.ledger.javaapi.data.Filter;
import com.daml.ledger.javaapi.data.InclusiveFilter;
import com.rln.client.damlClient.AutoApproveParameters;
import com.rln.client.damlClient.AutoApproveParameters.ApprovalMode;
import com.rln.client.damlClient.RLNClient;
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveTransferProposalMarker;
import com.rln.gui.backend.implementation.balanceManagement.cache.AutoApproveCache;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.model.ApprovalProperties;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AutoapproveApiImpl {

  private static final Filter AUTO_APPROVE_MARKER_FILTER = new InclusiveFilter(
    Set.of(AutoApproveTransferProposalMarker.TEMPLATE_ID), Map.of());

  private final GuiBackendConfiguration guiBackendConfiguration;
  private final AutoApproveCache autoApproveCache;
  private final RLNClient rlnClient;

  public AutoapproveApiImpl(GuiBackendConfiguration guiBackendConfiguration, AutoApproveCache autoApproveCache, RLNClient rlnClient) {
    this.guiBackendConfiguration = guiBackendConfiguration;
    this.autoApproveCache = autoApproveCache;
    this.rlnClient = rlnClient;
  }

  // TODO Looks like this endpoint is gone?
//  @Override
//  public List<AutoapproveListInner> apiAutoapproveListGet() {
//    var getAutoApproveMarkers = rlnClient.getActiveContracts(new FiltersByParty(
//      Collections.singletonMap(guiBackendConfiguration.partyId(), AUTO_APPROVE_MARKER_FILTER)));
//    return getAutoApproveMarkers.map(TransferProposalToApiTypeConverter::createdEventToAutoapproveListInner).toList().blockingGet();
//  }

  public void updateApprovalProperties(@Valid @NotNull ApprovalProperties autoApprove) {
    var marker = autoApproveCache.getMarker(autoApprove.getAddress());
    var markerId = marker != null ? marker.id : null;
    var now = Instant.now();
    // What happens, when username is NOT the current party (in the name of which the GUI backend is running)?
    var parameters = new AutoApproveParameters(
      now,
      guiBackendConfiguration.partyId(),
      autoApprove.getAddress(),
      markerId,
      convertApprovalMode(autoApprove),
      autoApprove.getLimit()
    );
    rlnClient.createOrUpdateAutoApproveMarker(parameters);
  }

  private ApprovalMode convertApprovalMode(ApprovalProperties autoApprove) {
    switch (autoApprove.getApprovalMode()) {
      case AUTO:
        return ApprovalMode.AUTO;
      case LIMIT:
        return ApprovalMode.LIMIT;
      default:
        return ApprovalMode.MANUAL;
    }
  }
}
