#!/bin/sh

echo Waiting for Kafka-1 to be ready...
cub kafka-ready -b broker-scheduler:9090 1 60

echo Waiting for Kafka-2 to be ready...
cub kafka-ready -b broker-assembler:9090 1 60

echo Waiting for Kafka-3 to be ready...
cub kafka-ready -b broker-bankA:9090 1 60


echo Waiting for Kafka-4 to be ready...
cub kafka-ready -b broker-bankB:9090 1 60

