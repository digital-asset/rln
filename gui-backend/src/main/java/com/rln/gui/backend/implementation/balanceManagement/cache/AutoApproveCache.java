/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement.cache;

import com.rln.damlCodegen.workflow.transferproposal.AutoApproveTransferProposalMarker;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoApproveCache {

    private final Map<String, AutoApproveTransferProposalMarker.Contract> autoApproveMarkers = new ConcurrentHashMap<>();

    public AutoApproveTransferProposalMarker.Contract getMarker(String address) {
        return autoApproveMarkers.get(address);
    }

    public void update(String address, AutoApproveTransferProposalMarker.Contract marker) {
        autoApproveMarkers.put(address, marker);
    }
}
