/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

public interface PartyManager {
    String getParty(String BIC);
    String getBic(String partyId);
    boolean hasPartyId(String partyId);
    boolean hasBic(String BIC);
}
