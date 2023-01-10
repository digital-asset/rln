#!/usr/bin/env bash

if [[ $# -lt 1 ]] ; then
    echo 'Usage:'
    echo "${0} <FILENAME>"
    echo "Note that FILENAME is stored relative to 'publisher/input' directory."
    exit 1
fi

cd "$(dirname "$0")"

file="$1"

rm -f "${file:?}"

for i in {1..10000}
do
  echo '{ "groupId":"groupId'${i:?}'", "initiator" : "bankABic", "payload": "uri or stringified pac008 xml ('${i:?}')" }' >> "${file:?}"
done
