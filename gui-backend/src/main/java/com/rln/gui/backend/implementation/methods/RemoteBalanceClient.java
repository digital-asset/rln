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

public class RemoteBalanceClient {
  private static final String ADDRESS_PARAMETER = "address";
  private static final String API_GETLOCALBALANCE_ENDPOINT = "/api/getlocalbalance";

  @SneakyThrows
  public Stream<Balance> getRemoteBalance(String baseUrl, WalletAddressDTO walletAddressDTO) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(getRemoteBalanceUri(baseUrl, walletAddressDTO.getAddress()))
        .GET()
        .build();
    HttpResponse<InputStream> response = HttpClient
        .newHttpClient()
        .send(request, HttpResponse.BodyHandlers.ofInputStream());
    return new ObjectMapper()
        .readValue(response.body(), new TypeReference<List<Balance>>() {})
        .stream();
  }

  static URI getRemoteBalanceUri(String baseUrl, String address) {
    return URI.create(String
        .format("http://%s%s?%s=%s",
            baseUrl,
            API_GETLOCALBALANCE_ENDPOINT,
            ADDRESS_PARAMETER,
            address));
  }
}
