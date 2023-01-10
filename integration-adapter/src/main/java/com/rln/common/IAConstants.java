/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.common;

public class IAConstants {
  public static final String KAFKA_INITIATE_TRANSFER_SUBMITTER = "KafkaInitiateTransferSubmitter";
  public static final String KAFKA_TRANSACTION_MANIFEST_SUBMITTER = "KafkaTransactionManifestSubmitter";
  public static final String KAFKA_FINALIZE_REJECT_SETTLEMENT_SUBMITTER = "KafkaFinalizeRejectSettlementSubmitter";
  public static final String KAFKA_APPROVE_REJECT_PROPOSAL_SUBMITTER = "KafkaApproveRejectProposalSubmitter";
  public static final String KAFKA_TRANSFER_PROPOSAL_SUBMITTER = "KafkaTransferProposalSubmitter";

  public static final String SCHEDULER_SHARD_PARTY_READER = "SchedulerShardPartyReader";
  public static final String ASSEMBLER_SHARD_PARTY_READER = "AssemblerShardPartyReader";
  public static final String BANK_SHARD_PARTY_READER = "BankShardPartyReader";

  public static final String JSON_NULL_STRING = null;
}
