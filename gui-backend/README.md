## Running the app
Use the following command:
```
java -jar build/quarkus-app/quarkus-run.ja
```

## Configuration

See https://quarkus.io/guides/config-reference

Use the following to set host/port:
```
quarkus.http.port=
quarkus.http.host=
```
See application.properties for the rest of available configuration options:
```
# Daml Ledger host and port
daml.ledger.host=localhost
daml.ledger.port=9000

# The party in the name of which the GUI backend will run / act
rln.gui.backend.party-id=defaultParty
# When true, settlement happens on the Daml ledger
# False means off-ledger settlement for non-Daml participants.
rln.gui.backend.settle-on-ledger=true

# The following file should contain all the parties/shards for the scheduler entity.
rln.gui.backend.scheduler-shard-parties-config=src/main/resources/shard-parties-scheduler.config

# Batching settings
rln.gui.backend.number-of-ledger-submitter-threads=8
rln.gui.backend.ledger-batch-submission-max-msec=250
rln.gui.backend.ledger-batch-submission-max-size=50
```

## Versioning

- Dar version is like 1.0.0
- Jar version is: [DAR-VERSION]-[Subversion]
  - For example: 1.0.0-1
- Docker image version is a label, the same as the Jar version

### Creating a new version

1. Dar change: modify `src/main/daml.yaml`'s Dar version
2. Java change: modify `build.gradle` / subversion

Use `gradle clean build -x test` to build the image.
Use `gradle clean build dockerSave -x test` to get `build/rln-adapter-docker.img`.
With `docker images | grep rln`, one can check the available images.


## Testing
1. In gui-backend/examples/canton, execute:
```
start.sh
```

2. Get a party identifier.
```
daml ledger list-parties --host localhost --port 9000
```
Put for example Sandbox party into application.properties.
Actualize shard-parties.config with some (e.g. Sandbox) party.

3. Create a BIC in Navigator.
```
daml navigator server localhost 9000 --port 7500 --feature-user-management=false
```
Then create the BIC (use for example CHASUS33XXX).


4. In the gui-backend/examples folder:
```
java -jar ../build/quarkus-app/quarkus-run.jar
```


Stopping:
```
docker compose stop
docker compose rm ledger ledger-script
```
