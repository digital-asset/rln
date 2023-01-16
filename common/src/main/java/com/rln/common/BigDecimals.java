package com.rln.common;

import java.math.BigDecimal;

public class BigDecimals {
  public static boolean equals(BigDecimal x, BigDecimal y) {
    return 0 == x.compareTo(y);
  }
  public static boolean lessThan(BigDecimal x, BigDecimal y) {
    return x.compareTo(y) < 0;
  }
}
