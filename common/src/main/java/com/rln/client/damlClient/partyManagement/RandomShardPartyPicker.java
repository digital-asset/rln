/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import java.util.Random;

public class RandomShardPartyPicker {
  private final ShardPartyReader reader;
  private final Random rand;

  public RandomShardPartyPicker(ShardPartyReader reader) {
    this.reader = reader;
    this.rand = new Random();
  }

  public String pickRandomShardParty() {
    var shardParties = reader.getShardParties();
    return shardParties.get(rand.nextInt(shardParties.size()));
  }
}
