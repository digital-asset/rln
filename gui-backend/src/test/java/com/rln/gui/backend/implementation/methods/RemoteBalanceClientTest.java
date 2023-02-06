package com.rln.gui.backend.implementation.methods;

import com.rln.gui.backend.model.WalletAddressDTO;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpRequest;

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

  @Test
  void getBalanceRequest_adds_authorization_header() {
    var walletAddress = WalletAddressDTO.builder()
      .bearerToken("ABC123")
      .address("test-wallet")
      .build();

    var request = RemoteBalanceClient.getBalanceRequest("http://dummy", walletAddress);

    var authorizationHeader = assertHasHeader(request, "Authorization");
    Assertions.assertEquals("Bearer ABC123", authorizationHeader);
  }

  private static String assertHasHeader(HttpRequest request, String name) {
    var header = request.headers().firstValue(name);
    if (header.isPresent()) {
      return header.get();
    }
    AssertionFailureBuilder
      .assertionFailure()
      .reason("request did not have value for the specified header")
      .includeValuesInMessage(true)
      .actual(request.headers())
      .expected(name)
      .buildAndThrow();
    return null;
  }
}
