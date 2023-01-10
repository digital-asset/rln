#!/bin/sh

print_usage() {
  printf "Usage: ./ledger-parties-to-config.sh -o <filepath you that you want the config files to output to> \n"
}

while getopts ":o:" flag; do
    case "${flag}" in
        o)
            outputPath=${OPTARG}
            ;;
        *)
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${outputPath}" ]; then
    print_usage
    exit 1
fi


for entity in "bankA" "bankB" "scheduler" "assembler"
do
  jq -r 'to_entries|.[].value' /config/ledger-parties.json | grep "$entity.*" > "$outputPath/shard-parties-$entity".config
done
