#!/bin/sh

if [[ $# -lt 5 ]] ; then
    echo 'Usage:'
    echo "${0} <MODE> <LEDGER_HOST> <LEDGER_PORT> <BROKER_HOST_PORT> <ENTITY>"
    echo "MODE = ASSEMBLER | BANK | SCHEDULER"
    exit 1
fi
VOL_MOUNT=/config
"${VOL_MOUNT}/ledger-parties-to-config.sh" -o "${VOL_MOUNT}/"
sync

MODE="${1}"
LEDGER_HOST="${2}"
LEDGER_PORT="${3}"
BROKER_HOST_PORT="${4}"
ENTITY="${5}"

BANK_A_PARTIES_CONFIG="${VOL_MOUNT}/shard-parties-bankA.config"
BANK_B_PARTIES_CONFIG="${VOL_MOUNT}/shard-parties-bankB.config"
SCHEDULER_PARTIES_CONFIG="${VOL_MOUNT}/shard-parties-scheduler.config"
ASSEMBLER_PARTIES_CONFIG="${VOL_MOUNT}/shard-parties-assembler.config"

function setupBankConfig () {
  case "${ENTITY}" in
    "bankA")
      OWN_PARTIES_CONFIG="${BANK_A_PARTIES_CONFIG}" ;;
    "bankB")
      OWN_PARTIES_CONFIG="${BANK_B_PARTIES_CONFIG}" ;;
    *)
      echo "bank entity does not exist ${ENTITY}"
      exit 1 ;;
  esac
}

case "${MODE}" in
  "BANK")
    setupBankConfig
    ;;

  "SCHEDULER")
    OWN_PARTIES_CONFIG="${SCHEDULER_PARTIES_CONFIG}"
    ;;

  "ASSEMBLER")
    OWN_PARTIES_CONFIG="${ASSEMBLER_PARTIES_CONFIG}"
    ;;

  *)
    echo "Wrong mode:  ${MODE}"
    exit 1
    ;;
esac

BIC_READING_PARTY=$(head -n 1 "${OWN_PARTIES_CONFIG}")

cat << EOF
DAML_LEDGER_HOST=${LEDGER_HOST}
DAML_LEDGER_PORT=${LEDGER_PORT}
_PROD_KAFKA_BOOTSTRAP_SERVERS=${BROKER_HOST_PORT}
DAML_LEDGER_BANK_BIC_READING_PARTY_ID=${BIC_READING_PARTY}

ADAPTER_MODE=${MODE}

ADAPTER_BANK_SHARD_PARTIES_CONFIG=${OWN_PARTIES_CONFIG}
ADAPTER_SCHEDULER_SHARD_PARTIES_CONFIG=${SCHEDULER_PARTIES_CONFIG}
ADAPTER_ASSEMBLER_SHARD_PARTIES_CONFIG=${ASSEMBLER_PARTIES_CONFIG}
EOF
