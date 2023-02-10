/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.gui.backend.ods;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.DamlOptional;
import com.daml.ledger.javaapi.data.DamlRecord;
import com.daml.ledger.javaapi.data.Text;
import com.daml.ledger.javaapi.data.Value;
import com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal;
import com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil;
import com.rln.gui.backend.implementation.common.GuiBackendConstants;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Builder(access = AccessLevel.PACKAGE, toBuilder = true)
@Getter
public class TransferProposal {

  private String id;
  private String contractId;
  private String transactionId;
  private String groupId;
  private String messageId;
  private String address;
  private String partyCode;
  private String assetCode;
  private BigDecimal amount;
  private String status;

  public boolean isWaiting() {
    return GuiBackendConstants.WAITING_STATUS.equals(status);
  }

  private static final String GROUP_ID = "groupId";
  private static final String MESSAGE_ID = "messageId";
  private static final String OWNER = "owner";
  private static final String STEP = "step";
  private static final String SENDER = "sender";
  private static final String RECEIVER = "receiver";
  private static final String IBANS = "ibans";
  private static final String DELIVERY = "delivery";
  private static final String AMOUNT = "amount";
  private static final String LABEL = "label";

  public TransferProposal finalizeSettlement() {
    return new TransferProposal(id, contractId, transactionId, groupId, messageId, address,
        partyCode, assetCode, amount, GuiBackendConstants.SUCCESS_STATUS);
  }

  public TransferProposal rejectSettlement() {
    return new TransferProposal(id, contractId, transactionId, groupId, messageId, address,
        partyCode, assetCode, amount, GuiBackendConstants.REJECTED_STATUS);
  }

  public static Stream<TransferProposal> createFrom(CreatedEvent createdEvent) {
    var result = Stream.<TransferProposal>builder();
    var status = getTransactionProposalStatus(createdEvent);
    var arguments = createdEvent.getArguments();
    var groupId = getField(arguments, GROUP_ID, Value::asText).getValue();
    var messageId = getField(arguments, MESSAGE_ID, Value::asText).getValue();
    var owner = getField(arguments, OWNER, Value::asParty).getValue();
    var stepRecord = getField(arguments, STEP, Value::asRecord);
    var delivery = getField(stepRecord, DELIVERY, Value::asRecord);
    var amount = getField(delivery, AMOUNT, Value::asNumeric).getValue();
    var label = getField(delivery, LABEL, Value::asText).getValue();
    var proposal =
        TransferProposal.builder()
            .contractId(createdEvent.getContractId())
            .groupId(groupId)
            .messageId(messageId)
            .transactionId(groupId)
            .partyCode(owner)
            .status(status)
            .assetCode(label);
    senderOf(stepRecord).ifPresent(senderIBAN ->
        result.add(proposal
            .id(CompoundUniqueIdUtil.getCompoundUniqueId(CompoundUniqueIdUtil.Subject.SENDER,
                createdEvent.getContractId()))
            .address(senderIBAN)
            .amount(amount.negate())
            .build()));
    receiverOf(stepRecord).ifPresent(receiverIBAN ->
        result.add(proposal
            .id(CompoundUniqueIdUtil.getCompoundUniqueId(CompoundUniqueIdUtil.Subject.RECEIVER,
                createdEvent.getContractId()))
            .address(receiverIBAN)
            .amount(amount)
            .build()));
    return result.build();
  }

  static Optional<String> senderOf(DamlRecord settlementStep) {
    var ibans = getField(settlementStep, IBANS, Value::asVariant);
    switch (ibans.getConstructor()) {
      case "SenderAndReceiver":
        var record = ibans.getValue().asRecord().get();
        return Optional.of(getField(record, SENDER, Value::asText).getValue());
      case "SenderOnly":
        return ibans.getValue().asText().map(Text::getValue);
      default:
        return Optional.empty();
    }
  }

  static Optional<String> receiverOf(DamlRecord settlementStep) {
    var ibans = getField(settlementStep, IBANS, Value::asVariant);
    switch (ibans.getConstructor()) {
      case "SenderAndReceiver":
        var record = ibans.getValue().asRecord().get();
        return Optional.of(getField(record, RECEIVER, Value::asText).getValue());
      case "SenderOnly":
        return ibans.getValue().asText().map(Text::getValue);
      default:
        return Optional.empty();
    }
  }

  private static String getTransactionProposalStatus(CreatedEvent createdEvent) {
    if (com.rln.damlCodegen.workflow.transferproposal.TransferProposal.TEMPLATE_ID
        .equals(createdEvent.getTemplateId())) {
      return GuiBackendConstants.WAITING_STATUS;
    } else if (ApprovedTransferProposal.TEMPLATE_ID.equals(createdEvent.getTemplateId())) {
      return GuiBackendConstants.APPROVE_STATUS;
    } else {
      return GuiBackendConstants.REJECTED_STATUS;
    }
  }

  private static <T> T getField(DamlRecord arguments, String name,
      Function<Value, Optional<T>> asDamlType) {
    return asDamlType
        .apply(arguments.getFieldsMap().get(name))
        .orElseThrow(() -> new RuntimeException("Missing field: " + name));
  }
}
