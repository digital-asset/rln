package com.rln.gui.backend.implementation.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SetlClient {
  private long clientId;
  private String name;
  private String iban;
}
