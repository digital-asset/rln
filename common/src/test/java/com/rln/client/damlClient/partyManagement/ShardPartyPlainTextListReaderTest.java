/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;

class ShardPartyPlainTextListReaderTest {

  @Test
  void initializeFromNonExistingPathFails() {
    Path path = Path.of("? % dummy % ?");
    Assertions.assertThrows(
        FileNotFoundException.class,
        () -> ShardPartyPlainTextListReader.initializeFromPath(path));
  }

  @Test
  void emptyFileResultsInNoParties() {
    try(var input = new StringReader("")) {
      var sut = new ShardPartyPlainTextListReader(input);

      Assertions.assertEquals(0, sut.getShardParties().size());
    }
  }

  @Test
  void singleLineFileResultsInOneParty() {
    var party = "party";
    try (var input = new StringReader(party)) {
      var sut = new ShardPartyPlainTextListReader(input);

      MatcherAssert.assertThat(sut.getShardParties(), Matchers.contains(party));
    }
  }

  @Test
  void multiLineFileResultsInParties() {
    var party1 = "partyA";
    var party2 = "partyB";
    var party3 = "partyC";
    var multiLine = String.join("\n", List.of(party1, party2, party3));
    try (var input = new StringReader(multiLine)) {
      var sut = new ShardPartyPlainTextListReader(input);

      MatcherAssert.assertThat(sut.getShardParties(), Matchers.contains(party1, party2, party3));
    }
  }

  @Test
  void emptyLinesAreFiltered() {
    var party1 = "partyA";
    var party2 = "partyB";
    var multiLine = String.join("\n", List.of("", party1, "", "", "", party2));
    try (var input = new StringReader(multiLine)) {
      var sut = new ShardPartyPlainTextListReader(input);

      MatcherAssert.assertThat(sut.getShardParties(), Matchers.contains(party1, party2));
    }
  }
}
