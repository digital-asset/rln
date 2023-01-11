## IntelliJ Setup
Be sure to mark `build/generated` as Generated Sources Root (in the Project browser on the left, right click on the folder).

## Versioning

- Dar version is like 1.0.0
- Docker image version is a label: [DAR-VERSION]-[Subversion]
  - For example: 1.0.0-1

### Creating a new version

1. modify `src/main/daml.yaml`'s Dar version
2. modify `build.gradle` / imageVersion (Docker image version)
3. modify `docker/Dockerfile-integration-adapter` / version label (Docker image version)

Use `gradle clean build -x test` to build the image.
Use `gradle clean build dockerSave -x test` to get `build/rln-adapter-docker.img`.
With `docker images | grep rln`, one can check the available images.

## Architecture

The application has three modes:
- Bank (initating payments)
- Scheduler (creating a settlement chain / route)
- Assembler (doing the settlement when it has all the approvals)

In each mode, some of the following components are activated:
- Kafka side listener logic (input from Kafka)
- Ledger output, command submission
- Ledger listener (Daml contract/transaction input)
- Kafka side output/sender logic

![plot](./docs/RLN_Pilot_Flow_Chart.png)

Each "flow" has the following steps:
- listening/observing
- translation (Daml -> Kafka, Kafka -> Daml)
- processing (submission, exercise)

Read the *Implementation* section for further insights.

## Build

```
gradle clean build
```

Output:
- dar file (build folder)
- Quarkus app (build folder, quarkus-app/, no uber-jar or fat-jar!)
- Docker image in the local registry (rln-adapter)
  - to save this, use `gradle dockerSave` and see `build/rln-adapter-docker.img`

Use the `stagingtest/` folder and README.md, TESTING.md there to manually test the app.


## Implementation

This section contains notes about the implementation details.

### Beans
The application works with a bean hierarchy and the beans needed for the actual mode are instantiated then "collected" (they are dependencies of the processor). We use Producer methods mostly, to create the beans. One should have a look at the following components:
- Listeners that depend on all the other components (e.g. processors) and provide entry points and "roots" in the bean hierarchy
    - AdapterApplication, Daml Listeners' injection point (@Inject)
        - produced by DamlListenerProducer
    - com/rln/client/kafkaClient/incoming Kafka Listeners, see the @Incoming annotation
        - no producer method here, these are automatically managed beans
            - ApproveRejectKafkaListener
            - EnrichedSettlementChainKafkaListener
            - FinalizeKafkaListener
            - InitiationKafkaListener
- Processors, MessageProcessingProducer

### Messaging topics
In `application.properties`, note:
```
mp.messaging.incoming.{some-channel-name}.topic=message
```
This is the incoming Kafka topic containing the (Pacs008) messages. The name of the channel that handles this topic is `{some-channel-name}`.

### Kafka incoming channels and consumers
With Quarkus/SmallRye, one can easily define Kafka incoming channels with the @Incoming annotation on a function. Each class with such a function is an automatically managed bean (so one should not try to instantiate it with a producer @Produces function).
We have 1-1 such @Incoming annotated function for each incoming message type, so each such message type has its own adapter. Not every mode needs every adapter. We programmatically switch off the adapters not necessary. Actually, we just disable their in-channel. The only place where we can do this is in a ConfigSourceFactory:
1. we need access to the application properties to know the mode
2. we need to set the channel to disabled before it is started (otherwise it does not work) [~ ie. as close to the startup as possible]
A config source factory needs to be registered. See the documentation:
https://smallrye.io/docs/smallrye-config/config-sources/config-sources.html#_config_source_factory

### Using the adapter Docker image

Start it like this:

```
docker container run rln-adapter:latest
```

One can configure the container started from the RLN adapter Docker image the following way:
```
docker container run \
        --env-file=<path-to-file>\
        rln-adapter:latest
```
One can also use key/value pairs:
```
--env KEY=value
```

Note that the following rules apply for overwriting configuration properties with environment variables:
https://quarkus.io/guides/config-reference

Use the following parameter if port mapping is needed:
```
    -p <host-port>:<container-port> \

```

## Configuration

- daml.ledger.host, ledger host e.g. localhost
- daml.ledger.port, ledger port e.g. 9000
- adapter.mode: ASSEMBLER | BANK | SCHEDULER
- daml.ledger.bank-bic-reading-party-id, the party used for reading BankBIC-s
- adapter.bank-shard-parties-config, file with 1-1 bank shard party on each line
- adapter.scheduler-shard-parties-config, file with 1-1 scheduler shard party on each line
- adapter.assembler-shard-parties-config, file with 1-1 assembler shard party on each line
- %prod.kafka.bootstrap.servers, Kafka server (e.g. localhost:9092)
- Topic names:
    - rln.kafka.initiation.topic=initiation
    - rln.kafka.enriched.topic=enriched
    - rln.kafka.finalize-reject-settlement.topic=finalize-reject
    - rln.kafka.approve-reject-proposal.topic=approve-reject
    - rln.kafka.transfer-proposal.topic=transfer-proposal
    - rln.kafka.transaction-manifest.topic=transaction-manifest

See to following guide for different ways of overriding the defaults:
https://quarkus.io/guides/config-reference


CONFIDENTIAL © 2023 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
Any unauthorized use, duplication or distribution is strictly prohibited.
