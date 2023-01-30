package com.rln.gui.backend.implementation.methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rln.gui.backend.model.WalletAddressDTO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class RemoteOwnedAddressSupplier {

  private final Path remoteOwnedAddressConfig;
  private final List<WalletAddressDTO> remoteOwnedAddresses;

  public RemoteOwnedAddressSupplier(Path remoteOwnedAddressConfig) {
    this.remoteOwnedAddressConfig = remoteOwnedAddressConfig;
    this.remoteOwnedAddresses = readRemoteOwnedAddresses();
  }

  public List<WalletAddressDTO> getRemoteOwnedAddresses() {
    return remoteOwnedAddresses;
  }

  private List<WalletAddressDTO> readRemoteOwnedAddresses() {
    try {
      return new ObjectMapper().readValue(
          remoteOwnedAddressConfig.toFile(),
          new TypeReference<>() {}
      );
    } catch (IOException e) {
      throw new InternalServerError(e);
    }
  }
}
