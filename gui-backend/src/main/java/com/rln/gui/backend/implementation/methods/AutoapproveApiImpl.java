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
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveTransferProposalMarker.Contract;
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveType;
import com.rln.damlCodegen.workflow.transferproposal.autoapprovetype.FullAuto;
import com.rln.damlCodegen.workflow.transferproposal.autoapprovetype.LimitedMaxAmount;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.AutoApproveCache;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.model.ApprovalProperties;
import com.rln.gui.backend.model.LedgerAddressDTO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AutoapproveApiImpl {

  private static final Filter AUTO_APPROVE_MARKER_FILTER = new InclusiveFilter(
    Set.of(AutoApproveTransferProposalMarker.TEMPLATE_ID), Map.of());

  private final GuiBackendConfiguration guiBackendConfiguration;
  private final AutoApproveCache autoApproveCache;
  private final AccountCache accountCache;
  private final RLNClient rlnClient;

  public AutoapproveApiImpl(GuiBackendConfiguration guiBackendConfiguration, AutoApproveCache autoApproveCache, AccountCache accountCache, RLNClient rlnClient) {
    this.guiBackendConfiguration = guiBackendConfiguration;
    this.autoApproveCache = autoApproveCache;
    this.accountCache = accountCache;
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

  public Object post(LedgerAddressDTO ledgerAddressDTO) {
    return null;
  }

  public List<LedgerAddressDTO> get() {
    var accounts = accountCache.getAccounts();
    ArrayList<LedgerAddressDTO> result = new ArrayList<>(accounts.size());

    for (var account : accounts) {
      var autoApproval = autoApproveCache.getMarker(account);
      result.add(LedgerAddressDTO.builder()
          .isIBAN(true)
          .address(account)
          .bearerToken("")
          .clientId(0L)
          .approvalMode(convertApproveType(autoApproval))
          .approvalLimit(getLimit(autoApproval))
          .build());
    }

    return result;
  }

  private String convertApproveType(Contract autoApproval) {
    if (autoApproval == null) {
      return "MANUAL";
    }
    var autoApproveType = autoApproval.data.autoApproveType;
    if (autoApproveType instanceof FullAuto) {
      return "AUTO";
    } else if (autoApproveType instanceof LimitedMaxAmount) {
      return "LIMIT";
    } else {
      throw new RuntimeException("Unknown approve mode/type: " + autoApproveType);
    }
  }

  private Double getLimit(Contract autoApprove) {
    if (autoApprove != null) {
      var autoApproveType = autoApprove.data.autoApproveType;
      if (autoApproveType instanceof LimitedMaxAmount) {
        return ((LimitedMaxAmount) autoApproveType).bigDecimalValue.doubleValue();
      }
    }
    return null;
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
