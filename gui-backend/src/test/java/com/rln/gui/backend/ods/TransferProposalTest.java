package com.rln.gui.backend.ods;

import com.rln.damlCodegen.workflow.data.IBANs;
import com.rln.damlCodegen.workflow.data.Instrument;
import com.rln.damlCodegen.workflow.data.SettlementStep;
import com.rln.damlCodegen.workflow.data.ibans.ReceiverOnly;
import com.rln.damlCodegen.workflow.data.ibans.SenderAndReceiver;
import com.rln.damlCodegen.workflow.data.ibans.SenderOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

class TransferProposalTest {
  @Test
  void sender_of() {
    Assertions.assertEquals(
      Optional.of("ABC"),
      TransferProposal.senderOf(settlementStepWithIbans(new SenderAndReceiver("ABC", "XYZ")).toValue())
    );
    Assertions.assertEquals(
      Optional.of("ABC"),
      TransferProposal.senderOf(settlementStepWithIbans(new SenderOnly("ABC")).toValue())
    );
    Assertions.assertEquals(
      Optional.empty(),
      TransferProposal.senderOf(settlementStepWithIbans(new ReceiverOnly("ABC")).toValue())
    );
  }

  @Test
  void receiver_of() {
    Assertions.assertEquals(
      Optional.of("XYZ"),
      TransferProposal.receiverOf(settlementStepWithIbans(new SenderAndReceiver("ABC", "XYZ")).toValue())
    );
    Assertions.assertEquals(
      Optional.empty(),
      TransferProposal.receiverOf(settlementStepWithIbans(new ReceiverOnly("XYZ")).toValue())
    );
    Assertions.assertEquals(
      Optional.of("XYZ"),
      TransferProposal.receiverOf(settlementStepWithIbans(new SenderOnly("XYZ")).toValue())
    );
  }

  private static SettlementStep settlementStepWithIbans(IBANs ibans) {
    var dummyInstrument = new Instrument(BigDecimal.ZERO, "Dummy");
    return new SettlementStep(ibans, dummyInstrument);
  }
}
