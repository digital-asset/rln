package com.rln.gui.backend.implementation.methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rln.gui.backend.implementation.config.SetlParty;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SetlPartySupplier {

  private final Path setlPartiesConfig;
  private final Map<Long, SetlParty> parties;
  private final Map<String, SetlParty> damlPartyToSetlParty;

  public SetlPartySupplier(Path setlPartiesConfig) {
    this.setlPartiesConfig = setlPartiesConfig;
    this.parties = readSetlPartiesFromConfig();
    this.damlPartyToSetlParty = getParties().stream()
        .collect(Collectors.toMap(SetlParty::getDamlPartyId, i -> i));
  }

  public Collection<SetlParty> getParties() {
    return parties.values();
  }

  public SetlParty getSetlPartyBySetlPartyId(Long setlPartyId) {
    return parties.get(setlPartyId);
  }

  public SetlParty getSetlPartyByDamlParty(String damlPartyId) {
    return damlPartyToSetlParty.get(damlPartyId);
  }

  public Long getSetlPartyIdByDamlParty(String damlPartyId) {
    return Optional.ofNullable(damlPartyToSetlParty.get(damlPartyId))
        .map(SetlParty::getId)
        .orElse(null);
  }

  private Map<Long, SetlParty> readSetlPartiesFromConfig() {
    try {
      return new ObjectMapper()
          .readValue(
              setlPartiesConfig.toFile(),
              new TypeReference<List<SetlParty>>() {})
          .stream()
          .collect(Collectors.toMap(SetlParty::getId, s -> s));
    } catch (IOException e) {
      throw new InternalServerError(e);
    }
  }
}
