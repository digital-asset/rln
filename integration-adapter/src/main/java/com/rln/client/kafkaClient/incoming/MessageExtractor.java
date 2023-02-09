package com.rln.client.kafkaClient.incoming;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MessageExtractor {
  private static final Logger LOG = LoggerFactory.getLogger(MessageExtractor.class);

  public static <T> Optional<T> extractAs(String message, Class<T> targetType) {
    try {
      var result = new ObjectMapper().readValue(message, targetType);
      return Optional.of(result);
    } catch (JsonProcessingException e) {
      LOG.info("Received an unknown message: {}", message);
      return Optional.empty();
    }
  }
}
