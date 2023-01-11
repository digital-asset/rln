package com.rln.client.damlClient;

import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.codegen.Exercised;
import com.daml.ledger.javaapi.data.codegen.Update;

public class TestUtils {
  public static <A> ExerciseCommand toExerciseCommand(Update<Exercised<A>> update) {
    return update
        .commands()
        .get(0)
        .asExerciseCommand()
        .orElseThrow(() -> new RuntimeException("Cannot convert to exercise update."));
  }
}
