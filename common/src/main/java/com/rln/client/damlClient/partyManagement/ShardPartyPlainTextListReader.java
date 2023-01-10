/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/*
Example file content: parties separated by newline.

partyid1
partyid2
partyid3
...
 */
public class ShardPartyPlainTextListReader implements ShardPartyReader {
  private final BufferedReader reader;
  private List<String> shardParties;

  public static ShardPartyPlainTextListReader initializeFromPath(Path path) throws FileNotFoundException {
    FileReader fileReader = new FileReader(path.toFile());
    return new ShardPartyPlainTextListReader(fileReader);
  }

  public ShardPartyPlainTextListReader(Reader input) {
    this.reader = new BufferedReader(input);
    this.shardParties = null;
  }

  @Override
  public List<String> getShardParties() {
    if (shardParties == null) {
      shardParties = reader.lines().filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
    return shardParties;
  }
}
