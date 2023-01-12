/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ChangeBalanceParameters implements Parameters {
  private final String provider;
  private final String iban;
  private final BigDecimal change;

}
