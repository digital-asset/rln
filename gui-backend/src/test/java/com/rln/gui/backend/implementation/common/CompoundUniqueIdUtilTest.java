/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.implementation.common;

import com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil.Subject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CompoundUniqueIdUtilTest {

  private static final String SOME_CONTRACTID = "4xyz-de123-b$6%#";

  @Test
  void getCompoundUniqueIdWorksWithSender() {
    var result = CompoundUniqueIdUtil.getCompoundUniqueId(Subject.SENDER, "WAITING", SOME_CONTRACTID);
    Assertions.assertEquals("SENDER-WAITING-4xyz-de123-b$6%#", result);
  }

  @Test
  void getCompoundUniqueIdWorksWithReceiver() {
    var result = CompoundUniqueIdUtil.getCompoundUniqueId(Subject.RECEIVER, "SOMETHING", SOME_CONTRACTID);
    Assertions.assertEquals("RECEIVER-OTHER-4xyz-de123-b$6%#", result);
  }

  @Test
  void parseSenderAndContractId() {
    var result = CompoundUniqueIdUtil.parseCompoundIdParts("SENDER-APPROVED-" + SOME_CONTRACTID);
    Assertions.assertEquals(Subject.SENDER, result.subject);
    Assertions.assertEquals(CompoundUniqueIdUtil.Type.APPROVED, result.contractIdType);
    Assertions.assertEquals(SOME_CONTRACTID, result.contractId);
  }

  @Test
  void parseReceiverAndContractId() {
    var result = CompoundUniqueIdUtil.parseCompoundIdParts("RECEIVER-WAITING-" + SOME_CONTRACTID);
    Assertions.assertEquals(Subject.RECEIVER, result.subject);
    Assertions.assertEquals(CompoundUniqueIdUtil.Type.WAITING, result.contractIdType);
    Assertions.assertEquals(SOME_CONTRACTID, result.contractId);
  }

  @Test
  void parseNonExistingSubject() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
        () -> CompoundUniqueIdUtil.parseCompoundIdParts("BLAHBLAHBLAH-" + SOME_CONTRACTID));
    Assertions.assertEquals("No enum constant com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil.Subject.BLAHBLAHBLAH",
        thrown.getMessage());
  }

  @Test
  void parseNonExistingType() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
            () -> CompoundUniqueIdUtil.parseCompoundIdParts("SENDER-BLAHBLAHBLAH-" + SOME_CONTRACTID));
    Assertions.assertEquals("No enum constant com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil.Type.BLAHBLAHBLAH",
            thrown.getMessage());
  }

  @Test
  void parseNonCompoundUIdInput() {
    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
        () -> CompoundUniqueIdUtil.parseCompoundIdParts("non_compound_uid_input"));
    Assertions.assertEquals("Not a compoundUID: non_compound_uid_input",
        thrown.getMessage());
  }

  @Test
  void statusToType() {
    Assertions.assertEquals(CompoundUniqueIdUtil.Type.WAITING, CompoundUniqueIdUtil.statusToType("WAITING"));
    Assertions.assertEquals(CompoundUniqueIdUtil.Type.APPROVED, CompoundUniqueIdUtil.statusToType("APPROVED"));
    Assertions.assertEquals(CompoundUniqueIdUtil.Type.OTHER, CompoundUniqueIdUtil.statusToType("BLAHBLAHBLAH"));
  }
}