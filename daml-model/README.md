# Daml Models

This folder contains the Regulated Liability Network's Daml model.

## Overview
The Daml code is split into different modules separated into different folders. Find the explanation for the split up below.
``` sh
.
├── Model
│   ├── Balance.daml
│   ├── BankBIC.daml
│   └── Blacklist.daml
├── Tests
│   ├── AutoApproveTriggerTest.daml
│   ├── BalanceManagementTriggerTest.daml
│   ├── BankBICsTest.daml
│   ├── Onboarding.daml
│   ├── PartyAlloc.daml
│   ├── StagingTest.daml
│   └── TransferWorkflow.daml
├── Trigger
│   ├── AutoApprove.daml
│   └── BalanceManagement.daml
└── Workflow
    ├── Data.daml
    ├── InitiateTransfer.daml
    ├── Instruction.daml
    ├── TransactionManifest.daml
    └── TransferProposal.daml
```
### Model Folder
The `Model` folder holds logic required by the Daml participant.
It contains the code required for managing the balances (`Balance.daml`), as well as the ability for basic sanctions checking (`Blacklist.daml`).
`BankBIC.daml` holds the data structure to be able to map from BICs to Daml specific Party Identifiers. It is also already laid out for sharding.
### Tests Folder
Various Tests are held here. You can run them by executing `daml test` (An installed Daml SDK is a prerequisite).
### Trigger Folder
Triggers are automations in Daml.
The `AutoApprove.daml` trigger looks for Daml contracts that hold its configuration and auto approves all Transfer Proposals that fit within the given parameters (i.e. Correct Currency, Amount lower than the limit, and no entity in the settlement chain being on the blacklist if a blacklist exists).
The `BalanceManagement.daml` trigger automates the management of balances (i.e. actual, available, projected),
### Workflow Folder
The Workflow Folder holds the basic logic required for the RLN workflow.
- `Data.daml` holds data definitions used throughout the RLN workflows. Note, for now, the PAC.008 message is being treated by daml as an opaque data blob.
- `InitiateTransfer.daml` deals with the first two steps of the RLN workflow. The initiation of the transfer as well as the creation of transfer proposals by the scheduler. As discussed in the working groups in future iterations the existance of the scheduler will not be required. For now, however, it needs to identify the settlement path.
- `Instruction.daml` provides the logic for settlement once a transfer has been finalized. It is only used by Daml participants.
- `TransactionManifest.daml` holds the smart contract that informs the Assembler about the existence of a pending transfer, and provides details about who needs to provide approvals before it can be finalized.
- `TransferProposal.daml` informs every stakeholder about the transfer details and allows them to either approve or reject the transfer.

CONFIDENTIAL © 2023 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
Any unauthorized use, duplication or distribution is strictly prohibited.
