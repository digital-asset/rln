# Test with Docker Compose

See `README.md` in this folder about how you can start the application with Docker Compose.

## Test Individual Components

The Docker Compose setup includes the Integration Adapter in various configurations, along with Kafka and Ledger components. The steps in this section exist to 'simulate' the missing components—Bank SETL, Scheduler SETL, Assembler SETL.

An automated version of these steps can be found in `kafka/test-components.exp`.

1. Send an initiation message with BankA
See stagingtest/kafka/publisher/input/step1-init.txt
```
./publish.sh step1-init.txt initiation broker-bankA 9090
```
Check contracts:
```
docker compose run --rm ledger-script /bin/sh
daml repl /canton/rln/rln.dar --ledger-host ledger --ledger-port 5011
import Model.BankBIC
Some p <- find (\d -> d.displayName == Some "bankA") <$> listKnownParties
bankBics <- query @BankBIC p.party
debug bankBics
import DA.List
"bankABic" == (head bankBics)._2.bic
```

2. See the initiation message with Scheduler
```
./consume.sh initiation broker-scheduler 9090
```

3. Send an enriched message with Scheduler
See stagingtest/kafka/publisher/input/step3-enriched.txt
```
./publish.sh step3-enriched.txt enriched broker-scheduler 9090
```
Check contracts:
```
docker compose run --rm ledger-script /bin/sh
daml repl /canton/rln/rln.dar --ledger-host ledger --ledger-port 5031
import Workflow.TransferProposal
Some p <- find (\d -> d.displayName == Some "scheduler") <$> listKnownParties
proposals <- query @TransferProposal p.party
debug proposals
```

4.1. See the transfer proposals with the bank
```
./consume.sh transfer-proposal broker-bankA 9090
```
BankB won't get anything (see the legs).

See the transaction manifest with assembler
```
./consume.sh transaction-manifest broker-assembler 9090
```

5. Send Approval/Rejection from the bank
```
./publish.sh step5-approval.txt approve-reject broker-bankA 9090
```

5.1. See the approvals as Assembler
```
./consume.sh approve-reject broker-assembler 9090
```

5.2 Check Instruction contract for the on ledger settlement (messageId1 only)
```
docker compose run --rm ledger-script /bin/sh
daml repl /canton/rln/rln.dar --ledger-host ledger --ledger-port 5011
import Workflow.Instruction
Some p <- find (\d -> d.displayName == Some "bankA") <$> listKnownParties
instructions <- query @Instruction p.party
debug instructions
```

6. Send a finalized message as Assembler
```
./publish.sh step6-finalize.txt finalize-reject broker-assembler 9090
```

7. See the finalized message with the bank
```
./consume.sh finalize-reject broker-bankA 9090
```

## Load Testing

Often you will want to do load testing with a custom build. These steps may help:

1. Optionally, change the Docker image label in the `dockerBuild` and `dockerSave` steps of `build.gradle` and `stagingtest/docker-compose.yml`.
1. Run `./gradlew clean build dockerSave -x test` to generate a Docker image and have it loaded in Docker.

The next step would be to start the application with Docker Compose, as explained in `README.md`.

Finally, simple load testing can be performed like this:

1. Generate a good number of messages and store them in `publisher/input/`. E.g.: `publisher/input/generate-large-step1-input.sh large-step1-input.txt`.
1. Optionally, in one console observe output. E.g.: `./consume.sh initiation broker-scheduler 9090 | ts '[%Y-%m-%d %H:%M:%S]'`. (`ts` is included in the package `moreutils` and adds a timestamp to every line.)
1. Optionally, in another console observe log files. E.g.: `docker-compose logs -f integration-adapter-bankA`.
1. In yet another console, load the previously generated input. E.g.: `./publish_batch.sh large-step1-input.txt initiation broker-bankA 9090`.