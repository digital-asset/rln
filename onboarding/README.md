## OnBoarding: Allocate Parties and Create BankBICs
For integration adapter to work, we need to create the mapping from daml party ids to BIC codes.
The mapping should be described using the [BankBIC](../src/main/daml/Model/BankBIC.daml) contracts.
This module exists to assist with allocating parties and creating BankBIC contracts. 
We will provide examples input json files and daml script to show how parties and BankBIC contracts can be created
easily.

### Daml Script
The [daml script](../src/main/daml/Tests/Onboarding.daml) reads the entityName and BIC mapping from an 
[input json file](inputExample.json). Except for entityName and BIC we will also need to provide the participantName
for each entity, this is so we can deploy the daml script in a distributed manner with canton. The script will then
create one party for each entity on their participant node (that's why we need participantName) 
and create BankBIC using the party id of the party we just created along with the BIC provided in the input json.
Aside from that the script also creates one scheduler party and one assembler party on participants with names
"schedulerParticipant" and "assemblerParticipant"

### Participant Name and Participant Node Mapping
To run the daml script to allocate parties and create BankBIC on corresponding canton participant nodes, 
we need to provide the mapping between participantName and host/port of the canton participant node. 
See [participantsExample.json](participantsExample.json) for how to specify the mapping.


### Run Daml Script with Canton
After we got the input json and the participant json ready, 
to run the [daml script](../src/main/daml/Tests/Onboarding.daml), follow the below command <br>
```
daml script --dar ../build/daml/rln.dar \
--script-name Tests.Onboarding:onboarding \
--participant-config participantsExample.json \
--input-file inputExample.json \
--output-file list-parties.json
```
The rln.dar file can be downloaded from [git release page](https://github.com/DACH-NY/proj-rln/releases/).
The --output-file option is the file that the script will dump all party ids created using the script to which includes
all bank entity specified in the input.json plus one scheduler party and one assembler party.
