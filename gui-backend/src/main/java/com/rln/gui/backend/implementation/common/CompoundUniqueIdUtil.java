/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.common;

import com.rln.damlCodegen.da.types.Tuple2;

public class CompoundUniqueIdUtil {

  public enum Subject {
    SENDER, RECEIVER
  }

  private static final String COMPOUND_ID_SEPARATOR = "-";

  public static String getCompoundUniqueId(Subject subject, String contractId) {
    return String.format("%s-%s", subject, contractId);
  }

  public static Tuple2<Subject, String> parseSubjectAndContractId(String compoundUID) {
    var result = compoundUID.split(COMPOUND_ID_SEPARATOR, 2);
    if (result.length != 2) {
      throw new RuntimeException("Not a compoundUID: " + compoundUID);
    }
    return new Tuple2<>(Subject.valueOf(result[0]), result[1]);
  }
}
