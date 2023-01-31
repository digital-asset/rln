package com.rln.gui.backend.implementation.config;

import java.util.List;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SetlParty {

  private String baseUrl;
  private Long id;
  private String damlPartyId;
  private String name;
  private List<SetlClient> clients;

  public SetlClient getSetlClient(String clientName) {
    return clients.stream()
        .filter(client -> client.getName().equals(clientName))
        .findFirst()
        .orElseThrow(noSuchClient(clientName));
  }

  private Supplier<RuntimeException> noSuchClient(String clientName) {
    return () -> new RuntimeException(
        String.format("Setl Party %s has no such client: %s", name, clientName));
  }
}
