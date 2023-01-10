/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

class RandomShardPartyPickerTest {
  @Test
  void picksRandomValues() {
    String party1 = "XYZ";
    String party2 = "ABC";
    List<String> testParties = List.of(party1, party2);
    ShardPartyReader reader = () -> testParties;
    var sut = new RandomShardPartyPicker(reader);

    var randomParty = sut.pickRandomShardParty();

    MatcherAssert.assertThat(randomParty, Matchers.anyOf(Matchers.equalTo(party1), Matchers.equalTo(party2)));
  }
}
