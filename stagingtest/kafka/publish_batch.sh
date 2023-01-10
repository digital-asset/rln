#!/bin/bash

if [[ $# -lt 2 ]] ; then
    echo 'Usage:'
    echo "${0} <FILENAME> <TOPICNAME> [BROKER] [PORT]"
    echo "Note that FILENAME has to be a file under the 'publisher/input' directory."
    exit 1
fi

FILENAME="${1}"
TOPIC="${2}"
BROKER="${3:-broker}"
PORT="${4:-9092}"

docker compose exec "${BROKER}" python /publisher/publish_batch.py "/publisher/input/${FILENAME}" "${TOPIC}" "${BROKER}" "${PORT}"
