/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.common;

import java.util.Objects;

public class CompoundUniqueIdUtil {

  public static class CompoundUniqueIdParts {
    public final Subject subject;
    public final Type contractIdType;
    public final String contractId;

    public CompoundUniqueIdParts(Subject subject, Type contractIdType, String contractId) {
      this.subject = subject;
      this.contractIdType = contractIdType;
      this.contractId = contractId;
    }
  }

  public enum Subject {
    SENDER, RECEIVER
  }

  public enum Type {
    WAITING, APPROVED, OTHER
  }

  private static final String COMPOUND_ID_SEPARATOR = "-";

  public static String getCompoundUniqueId(Subject subject, String status, String contractId) {
    return String.format("%s-%s-%s", subject, statusToType(status), contractId);
  }

  public static Type statusToType(String status) {
    if (Objects.equals(Type.WAITING.name(), status)) {
      return Type.WAITING;
    } else if (Objects.equals(Type.APPROVED.name(), status)) {
      return Type.APPROVED;
    } else {
      return Type.OTHER;
    }
  }

  public static CompoundUniqueIdParts parseCompoundIdParts(String compoundUID) {
    var result = compoundUID.split(COMPOUND_ID_SEPARATOR, 3);
    if (result.length != 3) {
      throw new RuntimeException("Not a compoundUID: " + compoundUID);
    }
    return new CompoundUniqueIdParts(Subject.valueOf(result[0]), Type.valueOf(result[1]), result[2]);
  }
}
