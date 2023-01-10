#!/usr/bin/env bash

rln_models=../../../daml-model/build/daml/rln.dar

daml sandbox \
  --port 9000 \
  --dar "$rln_models"
