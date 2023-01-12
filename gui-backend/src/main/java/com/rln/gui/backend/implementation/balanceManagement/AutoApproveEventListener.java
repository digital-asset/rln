/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.balanceManagement;

import com.daml.ledger.javaapi.data.ArchivedEvent;
import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Event;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.codegen.ValueDecoder;
import com.rln.client.damlClient.RLNClient;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.workflow.transferproposal.AutoApproveTransferProposalMarker;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.AutoApproveCache;
import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoApproveEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AutoApproveEventListener.class);
    private static final Set<Identifier> autoApproveTemplates = Set.of(
        AutoApproveTransferProposalMarker.TEMPLATE_ID);
    private static final ValueDecoder<AutoApproveTransferProposalMarker> VALUE_DECODER =
        AutoApproveTransferProposalMarker.valueDecoder();
    private final AutoApproveCache autoApproveCache;

    public AutoApproveEventListener(RLNClient rlnClient, String bankParty, AutoApproveCache autoApproveCache) {
        this.autoApproveCache = autoApproveCache;
        rlnClient.subscribeForContinuousEvent(bankParty, autoApproveTemplates, this::updateAccountWithEvent);
    }

    private void updateAccountWithEvent(Event event) {
        if (event instanceof ArchivedEvent) {
            return;
        }
        logger.info("Update Autoapprove with {}", event);
        var createdEvent = (CreatedEvent) event;
        var marker = VALUE_DECODER.decode(createdEvent.getArguments());
        autoApproveCache.update(marker.address, AutoApproveTransferProposalMarker.Contract.fromCreatedEvent(createdEvent));
    }
}
