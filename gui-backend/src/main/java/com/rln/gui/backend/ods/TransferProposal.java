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
import com.daml.ledger.javaapi.data.Variant;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.workflow.data.ibans.SenderAndReceiver;
import com.rln.damlCodegen.workflow.transferproposal.ApprovedTransferProposal;
import com.rln.gui.backend.implementation.common.CompoundUniqueIdUtil;
import com.rln.gui.backend.implementation.common.GuiBackendConstants;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Builder(access = AccessLevel.PRIVATE)
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

  public static Stream<TransferProposal> createFrom(CreatedEvent createdEvent) {
    var result = Stream.<TransferProposal>builder();
    var status = getTransactionProposalStatus(createdEvent);
    var arguments = createdEvent.getArguments();
    var groupId = getField(arguments, GROUP_ID, Value::asText).getValue();
    var messageId = getField(arguments, MESSAGE_ID, Value::asText).getValue();
    var owner = getField(arguments, OWNER, Value::asParty).getValue();
    var stepRecord = getField(arguments, STEP, Value::asRecord);
    var ibans = getField(stepRecord, IBANS, Value::asVariant);
    var delivery = getField(stepRecord, DELIVERY, Value::asRecord);
    var amount = getField(delivery, AMOUNT, Value::asNumeric).getValue();
    var label = getField(delivery, LABEL, Value::asText).getValue();
    var senderAndReceiver = getSenderAndReceiver(ibans);
    var proposal =
      TransferProposal.builder()
        .contractId(createdEvent.getContractId())
        .groupId(groupId)
        .messageId(messageId)
        .transactionId("")
        .partyCode(owner)
        .status(status)
        .assetCode(label);
    senderAndReceiver._1.ifPresent(senderIBAN ->
      result.add(proposal
        .id(CompoundUniqueIdUtil.getCompoundUniqueId(CompoundUniqueIdUtil.Subject.SENDER, createdEvent.getContractId()))
        .address(senderIBAN)
        .amount(amount.negate())
        .build()));
    senderAndReceiver._2.ifPresent(receiverIBAN ->
      result.add(proposal
        .id(CompoundUniqueIdUtil.getCompoundUniqueId(CompoundUniqueIdUtil.Subject.RECEIVER, createdEvent.getContractId()))
        .address(receiverIBAN)
        .amount(amount)
        .build()));
    return result.build();
  }

  private static Tuple2<Optional<String>, Optional<String>> getSenderAndReceiver(Variant ibans) {
    var value = ibans.getValue();
    if (ibans.getConstructor().equals("SenderAndReceiver")) {
      var record = value.asRecord().get();
      var sender = Optional.of(getField(record, SENDER, Value::asText).getValue());
      var receiver = Optional.of(getField(record, RECEIVER, Value::asText).getValue());
      return new Tuple2<>(sender, receiver);
    } else if (ibans.getConstructor().equals("SenderOnly")) {
      return new Tuple2<>(value.asText().map(Text::getValue), Optional.empty());
    } else {  // ibans.getConstructor().equals("ReceiverOnly")
      return new Tuple2<>(Optional.empty(), value.asText().map(Text::getValue));
    }
  }

  private static String getTransactionProposalStatus(CreatedEvent createdEvent) {
    if (com.rln.damlCodegen.workflow.transferproposal.TransferProposal.TEMPLATE_ID.equals(createdEvent.getTemplateId())) {
      return GuiBackendConstants.WAITING_STATUS;
    } else if (ApprovedTransferProposal.TEMPLATE_ID.equals(createdEvent.getTemplateId())) {
      return GuiBackendConstants.SUCCESS_STATUS;
    } else {
      return GuiBackendConstants.REJECTED_STATUS;
    }
  }

  private static Optional<String> toOptionalString(DamlOptional optional) {
    return optional
      .toOptional(v ->
        v.asText()
          .orElseThrow(() -> new RuntimeException("There is no text in the Daml optional value."))
          .getValue());
  }

  private static <T> T getField(DamlRecord arguments, String name, Function<Value, Optional<T>> asDamlType) {
    return asDamlType
      .apply(arguments.getFieldsMap().get(name))
      .orElseThrow(() -> new RuntimeException("Missing field: " + name));
  }
}
