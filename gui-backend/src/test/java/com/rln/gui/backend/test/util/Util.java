package com.rln.gui.backend.test.util;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import java.math.BigDecimal;

public class Util {
    public static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        MatcherAssert.assertThat(actual, Matchers.comparesEqualTo(expected));
    }
}
