package com.rln.gui.backend.implementation.config;

import java.util.List;
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
}
