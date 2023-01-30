package com.rln.gui.backend.implementation.config;

import com.rln.gui.backend.model.WalletAddressDTO;

public class WalletAddress extends WalletAddressDTO {

  public WalletAddress() {
    // To allow JSON parsing
    super("", "", 1L, 1L);
  }
}
