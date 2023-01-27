package com.rln.gui.backend.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class EventuallyTest {
  @Test
  void eventually_returns_when_code_succeeds() {
    var success = new AtomicBoolean(false);
    Assertions.assertDoesNotThrow(() -> Eventually.eventually(
      () -> success.set(true), 1, Duration.ZERO, Duration.ofMillis(100)
    ));
    Assertions.assertTrue(success.get());
  }

  @Test
  void eventually_returns_when_code_succeeds_within_attempts() {
    Assertions.assertDoesNotThrow(() -> Eventually.eventually(
      codeThatFailsForGivenAttempts(3),
      4, Duration.ZERO, Duration.ofMillis(100)
    ));
    Assertions.assertThrows(AssertionFailedError.class, () -> Eventually.eventually(
      codeThatFailsForGivenAttempts(3),
      3, Duration.ZERO, Duration.ofMillis(100)
    ));
    Assertions.assertThrows(AssertionFailedError.class, () -> Eventually.eventually(
      codeThatFailsForGivenAttempts(3),
      2, Duration.ZERO, Duration.ofMillis(100)
    ));
  }

  @Test
  void eventually_returns_when_code_succeeds_within_time() {
    Assertions.assertDoesNotThrow(() -> Eventually.eventually(
      codeThatFailsForGivenMillis(100),
      Integer.MAX_VALUE, Duration.ZERO, Duration.ofMillis(200)
    ));
    Assertions.assertThrows(AssertionFailedError.class, () -> Eventually.eventually(
      codeThatFailsForGivenMillis(100),
      Integer.MAX_VALUE, Duration.ZERO, Duration.ofMillis(50)
    ));
  }

  @Test
  void codeThatFailsForGivenAttempts_test() {
    var code = codeThatFailsForGivenAttempts(2);
    Assertions.assertThrows(RuntimeException.class, code);
    Assertions.assertThrows(RuntimeException.class, code);
    Assertions.assertDoesNotThrow(code);
  }

  private Executable codeThatFailsForGivenAttempts(int attemptsToFail) {
    var attempts = new AtomicInteger(1);
    return () -> {
      if (attempts.getAndIncrement() <= attemptsToFail) simulateFailure();
    };
  }

  @Test
  void codeThatFailsForGivenMillis_test() {
    var code = codeThatFailsForGivenMillis(50);
    Assertions.assertThrows(RuntimeException.class, code);
    Assertions.assertThrows(RuntimeException.class, () -> {
      Thread.sleep(25);
      code.execute();
    });
    Assertions.assertDoesNotThrow(() -> {
      Thread.sleep(40);
      code.execute();
    });
  }

  private Executable codeThatFailsForGivenMillis(long millis) {
    var start = System.currentTimeMillis();
    return () -> {
      if (System.currentTimeMillis() - start <= millis) simulateFailure();
    };
  }

  private static void simulateFailure() {
    throw new RuntimeException("Simulated failure.");
  }
}
