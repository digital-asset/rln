package com.rln.gui.backend.implementation.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SetlParty {
  private String baseUrl;
  private Long id;
  private String damlPartyId;
  private String name;
}
