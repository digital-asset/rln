package com.rln.gui.backend.implementation.methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.implementation.config.SetlParty;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SetlPartySupplier {

  private final Path setlPartiesConfig;
  private final List<SetlParty> parties;
  private final Map<String, Long> damlPartyToSetlId;

  public SetlPartySupplier(Path setlPartiesConfig) {
    this.setlPartiesConfig = setlPartiesConfig;
    this.parties = readSetlPartiesFromConfig();
    this.damlPartyToSetlId = getParties().stream()
        .collect(Collectors.toMap(SetlParty::getDamlPartyId, SetlParty::getId));
  }

  public List<SetlParty> getParties() {
    return this.parties;
  }

  public Long getSetlId(String damlPartyId) {
    return damlPartyToSetlId.get(damlPartyId);
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
