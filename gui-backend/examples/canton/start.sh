#!/usr/bin/env bash

rln_models=../../../daml-model/build/daml/rln-2.0.0.dar

daml sandbox \
  --port 9000 \
  --dar "$rln_models"
