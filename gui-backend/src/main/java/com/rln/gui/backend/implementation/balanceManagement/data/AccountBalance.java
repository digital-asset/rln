package com.rln.gui.backend.implementation.balanceManagement.data;

import lombok.Getter;

import javax.validation.constraints.NotNull;
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

    public AccountBalance(
            AccountInfo accountInfo,
            String assetName,
            String address,
            BigDecimal liquid,
            BigDecimal incoming,
            BigDecimal locked) {
        Objects.requireNonNull(liquid);
        Objects.requireNonNull(incoming);
        Objects.requireNonNull(locked);
        this.accountInfo = accountInfo;
        this.assetName = assetName;
        this.address = address;
        this.liquid = liquid;
        this.incoming = incoming;
        this.locked = locked;
    }

    public String getProvider() {
        return accountInfo.getProvider();
    }

    public String getOwner() {
        return accountInfo.getOwner();
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
