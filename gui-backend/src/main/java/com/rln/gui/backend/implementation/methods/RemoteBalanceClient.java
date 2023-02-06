package com.rln.gui.backend.implementation.methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rln.gui.backend.model.Balance;
import com.rln.gui.backend.model.WalletAddressDTO;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;

import javax.ws.rs.core.UriBuilder;

public class RemoteBalanceClient {
  private static final String ADDRESS_PARAMETER = "address";
  private static final String API_GET_LOCAL_BALANCE_ENDPOINT = "/api/getlocalbalance";

  @SneakyThrows
  public Stream<Balance> getRemoteBalance(String baseUrl, WalletAddressDTO walletAddressDTO) {
    HttpRequest request = getBalanceRequest(baseUrl, walletAddressDTO);
    HttpResponse<InputStream> response = HttpClient
        .newHttpClient()
        .send(request, HttpResponse.BodyHandlers.ofInputStream());
    return new ObjectMapper()
        .readValue(response.body(), new TypeReference<List<Balance>>() {})
        .stream();
  }

  static HttpRequest getBalanceRequest(String baseUrl, WalletAddressDTO walletAddressDTO) {
    return HttpRequest.newBuilder()
      .uri(getRemoteBalanceUri(baseUrl, walletAddressDTO.getAddress()))
      .header("Authorization", "Bearer " + walletAddressDTO.getBearerToken())
      .GET()
      .build();
  }

  static URI getRemoteBalanceUri(String baseUrl, String address) {
    return UriBuilder
      .fromUri(baseUrl)
      .path(API_GET_LOCAL_BALANCE_ENDPOINT)
      .queryParam(ADDRESS_PARAMETER, address)
      .build();
  }
}
