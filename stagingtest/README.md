Launch by running:
```
docker compose --profile "kafka" up -d
docker compose --profile "ledger" up -d
docker compose --profile "ia" up -d
```

To start listening at a topic:
```
./consume.sh orders broker-bankB 9090
```
This will print messages starting at the beginning of the Kafka topic. If you run this command more than once, it will print all messages in all sessions.

To start publishing messages:
```
./publish.sh example.txt orders broker-bankB 9090
```
Note that the input file (e.g. `example.txt`) has to be in the `publisher/input` folder.

To check Canton with canton console
```
docker compose exec -it ledger sh
# In Canton container
bin/canton -c rln/remote-admin.conf

# In Canton console @canton
remoteParticipantBankA.ledger_api.acs.of_all() <- this will return the full list of active contracts, can map, filter to find the required data
# Example to filter the active contracts by template name BankBIC
remoteParticipantBankA.ledger_api.acs.of_all().map(e => e.event).filter(event => event.templateId.get.entityName.contains("BankBIC")
```

See `TESTING.md` for staging testing instructions.
