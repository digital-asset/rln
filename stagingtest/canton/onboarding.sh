#!/bin/bash
set -e

function wait_for {
  while ! daml ledger list-parties --host ledger --port "${1}"
  do
    echo "Waiting for Sandbox to start..."
    sleep 5
  done
}

echo "Sandbox started"
DAR="/canton/rln/rln.dar"

wait_for 5011
wait_for 5021
wait_for 5031
wait_for 5041

daml ledger upload-dar "${DAR}" --host ledger --port 5011
daml ledger upload-dar "${DAR}" --host ledger --port 5021
daml ledger upload-dar "${DAR}" --host ledger --port 5031
daml ledger upload-dar "${DAR}" --host ledger --port 5041

daml script --dar "${DAR}" \
 --script-name Tests.StagingTest:stagingTest \
 --participant-config /canton/rln/participants.json \
 --output-file /canton/rln/config/ledger-parties.json

echo "Onboarding done."
