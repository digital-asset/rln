#!/bin/bash

if [[ $# -lt 1 ]] ; then
    echo 'Usage:'
    echo "${0} <TOPICNAME> [BROKER] [PORT]"
    exit 1
fi

TOPIC="${1}"
BROKER="${2:-broker}"
PORT="${3:-9092}"

docker compose exec -T "${BROKER}" bash -c "echo 'Listening at topic ${TOPIC} (${BROKER}:${PORT})...' &&
kafka-console-consumer \
  --from-beginning \
  --topic ${TOPIC} \
  --bootstrap-server ${BROKER}:${PORT}"
