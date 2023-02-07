package com.rln.gui.backend.implementation.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SetlTreasuryAccount {
    private long clientId;
    private String provider;
    private String iban;
    private String bearerToken;
}
