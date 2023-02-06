package com.rln.gui.backend.implementation.methods;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RemoteBalanceClientTest {
  @Test
  void test_getRemoteBalanceUri() {
    var uri = RemoteBalanceClient.getRemoteBalanceUri("http://" + LedgerBaseTest.BASEURL, LedgerBaseTest.SENDER_IBAN);
    Assertions.assertEquals("http://baseurl/api/getlocalbalance?address=SENDER_IBAN", uri.toString());

    uri = RemoteBalanceClient.getRemoteBalanceUri("http://" + LedgerBaseTest.BASEURL + "/", LedgerBaseTest.SENDER_IBAN);
    Assertions.assertEquals("http://baseurl/api/getlocalbalance?address=SENDER_IBAN", uri.toString());

    uri = RemoteBalanceClient.getRemoteBalanceUri("https://" + LedgerBaseTest.BASEURL + "/", LedgerBaseTest.SENDER_IBAN);
    Assertions.assertEquals("https://baseurl/api/getlocalbalance?address=SENDER_IBAN", uri.toString());

    uri = RemoteBalanceClient.getRemoteBalanceUri("https://" + LedgerBaseTest.BASEURL + "/", LedgerBaseTest.SENDER_IBAN);
    Assertions.assertEquals("https://baseurl/api/getlocalbalance?address=SENDER_IBAN", uri.toString());
  }
}
