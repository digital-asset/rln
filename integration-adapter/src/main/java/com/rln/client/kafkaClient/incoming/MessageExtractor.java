package com.rln.client.kafkaClient.incoming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MessageExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(MessageExtractor.class);

  public static <T> Optional<T> extractPayloadAs(Message<String> message, Class<T> targetType) {
    var metadata = message.getMetadata();
    var payload = message.getPayload();
    try {
      var result = new ObjectMapper().readValue(payload, targetType);
      return Optional.of(result);
    } catch (JsonProcessingException e) {
      LOG.info("Received an unknown message. Metadata: {}, payload: {}", metadata, payload);
      return Optional.empty();
    }
  }
}
