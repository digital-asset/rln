package com.rln.gui.backend.implementation.methods;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RemoteOwnedAddressSupplierTest {
  static private final Path configFile = Path.of("src/test/resources/remote-owned-addresses.json");
  static private final RemoteOwnedAddressSupplier remoteOwnedAddressSupplier = new RemoteOwnedAddressSupplier(configFile);

  @Test
  void getRemoteOwnedAddresses() {
    MatcherAssert.assertThat(remoteOwnedAddressSupplier.getRemoteOwnedAddresses(), Matchers.hasSize(1));
  }
}