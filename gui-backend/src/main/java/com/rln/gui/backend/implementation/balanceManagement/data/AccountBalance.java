package com.rln.gui.backend.implementation.balanceManagement.data;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class AccountBalance {
    public static final Long ASSET_ID = 0L;
    private final AccountInfo accountInfo;
    private final String assetName;
    private final String address;
    private BigDecimal liquid;
    private final BigDecimal incoming;
    private final BigDecimal locked;
    private final String providerName;

    public AccountBalance(
            AccountInfo accountInfo,
            String providerName,
            String assetName,
            String address,
            BigDecimal liquid,
            BigDecimal incoming,
            BigDecimal locked) {
        Objects.requireNonNull(liquid);
        Objects.requireNonNull(incoming);
        Objects.requireNonNull(locked);
        this.accountInfo = accountInfo;
        this.providerName = providerName;
        this.assetName = assetName;
        this.address = address;
        this.liquid = liquid;
        this.incoming = incoming;
        this.locked = locked;
    }

    public String getOwnerName() {
        return accountInfo.getOwnerName();
    }

    public void addLiquid(BigDecimal change) {
        Objects.requireNonNull(change);
        liquid = liquid.add(change);
    }

    public boolean isAllZero() {
        return BigDecimal.ZERO.compareTo(liquid) == 0
                && BigDecimal.ZERO.compareTo(incoming) == 0
                && BigDecimal.ZERO.compareTo(locked) == 0;
    }
}
