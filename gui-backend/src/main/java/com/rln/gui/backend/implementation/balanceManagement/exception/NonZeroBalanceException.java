package com.rln.gui.backend.implementation.balanceManagement.exception;

public class NonZeroBalanceException extends RuntimeException {
  private static final long serialVersionUID = 123456711L;

  public NonZeroBalanceException(String iban) {
    super(String.format("Balance is not zero: %s.", iban));
  }
}
