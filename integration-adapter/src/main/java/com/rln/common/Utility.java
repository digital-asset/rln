package com.rln.common;

import com.rln.client.kafkaClient.message.fields.Step;
import com.rln.damlCodegen.workflow.data.IBANs;
import com.rln.damlCodegen.workflow.data.ibans.ReceiverOnly;
import com.rln.damlCodegen.workflow.data.ibans.SenderAndReceiver;
import com.rln.damlCodegen.workflow.data.ibans.SenderOnly;

public class Utility {
  public static IBANs toIBANs(Step step) {
    var sender = step.getSender();
    var receiver = step.getReceiver();
    if (sender == null && receiver == null) {
      throw new RuntimeException(String.format("Step missing both sender and receiver: %s", step));
    }
    if (sender == null) {
      return new ReceiverOnly(receiver);
    }
    if (receiver == null) {
      return new SenderOnly(sender);
    }
    return new SenderAndReceiver(sender, receiver);
  }
}
