package com.rln.gui.backend.implementation.methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.implementation.config.SetlParty;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SetlPartySupplier {

  private final Path setlPartiesConfig;
  private final List<SetlParty> parties;

  public SetlPartySupplier(Path setlPartiesConfig) {
    this.setlPartiesConfig = setlPartiesConfig;
    this.parties = readSetlPartiesFromConfig();
  }

  public List<SetlParty> getParties() {
    return this.parties;
  }

  private List<SetlParty> readSetlPartiesFromConfig() {
    try {
      return new ObjectMapper().readValue(
          setlPartiesConfig.toFile(),
          new TypeReference<>() {}
      );
    } catch (IOException e) {
      throw new InternalServerError(e);
    }
  }
}
