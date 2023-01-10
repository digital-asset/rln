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
    var result = CompoundUniqueIdUtil.getCompoundUniqueId(Subject.SENDER, SOME_CONTRACTID);
    Assertions.assertEquals("SENDER-4xyz-de123-b$6%#", result);
  }

  @Test
  void getCompoundUniqueIdWorksWithReceiver() {
    var result = CompoundUniqueIdUtil.getCompoundUniqueId(Subject.RECEIVER, SOME_CONTRACTID);
    Assertions.assertEquals("RECEIVER-4xyz-de123-b$6%#", result);
  }

  @Test
  void parseSenderAndContractId() {
    var result = CompoundUniqueIdUtil.parseSubjectAndContractId("SENDER-" + SOME_CONTRACTID);
    Assertions.assertEquals(Subject.SENDER, result._1);
    Assertions.assertEquals(SOME_CONTRACTID, result._2);
  }

  @Test
  void parseReceiverAndContractId() {
    var result = CompoundUniqueIdUtil.parseSubjectAndContractId("RECEIVER-" + SOME_CONTRACTID);
    Assertions.assertEquals(Subject.RECEIVER, result._1);
    Assertions.assertEquals(SOME_CONTRACTID, result._2);
  }

  @Test
  void parseNonExistingSubject() {
    IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
        () -> CompoundUniqueIdUtil.parseSubjectAndContractId("BLAHBLAHBLAH-" + SOME_CONTRACTID));
    Assertions.assertEquals("No enum constant com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil.Subject.BLAHBLAHBLAH",
        thrown.getMessage());
  }

  @Test
  void parseNonCompoundUIdInput() {
    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
        () -> CompoundUniqueIdUtil.parseSubjectAndContractId("non_compound_uid_input"));
    Assertions.assertEquals("Not a compoundUID: non_compound_uid_input",
        thrown.getMessage());
  }
}