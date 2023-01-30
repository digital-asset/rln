package com.rln.gui.backend.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;

public class Eventually {
  public static void eventually(Executable code) {
    eventually(
      code,
      50,
      Duration.ofMillis(100),
      Duration.ofSeconds(5)
    );
  }

  static void eventually(Executable code,
                         int maxAttempts,
                         Duration intervalBetweenAttempts,
                         Duration timeout) {
    var start = Instant.now();
    var failedAttempts = 0;
    var succeeded = false;
    Throwable lastError = null;
    while (!succeeded && failedAttempts < maxAttempts && !hasTimedOut(start, timeout)) {
      lastError = execute(code);
      succeeded = lastError == null;
      if (!succeeded) {
        failedAttempts++;
        sleep(intervalBetweenAttempts);
      }
    }
    if (!succeeded) {
      Assertions.fail(
        String.format("Code did not succeed after %d attempts within %s", failedAttempts, timeout),
        lastError
      );
    }
  }

  private static Throwable execute(Executable code) {
    try {
      code.execute();
      return null;
    } catch (Throwable e) {
      return e;
    }
  }

  private static boolean hasTimedOut(Instant start, Duration timeout) {
    return 0 <= passedTimeSince(start).compareTo(timeout);
  }

  private static Duration passedTimeSince(Temporal start) {
    return Duration.between(start, Instant.now());
  }

  private static void sleep(Duration sleep) {
    try {
      Thread.sleep(sleep.toMillis());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
