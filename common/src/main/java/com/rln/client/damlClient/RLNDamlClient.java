/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.Event;
import com.daml.ledger.javaapi.data.FiltersByParty;
import com.daml.ledger.javaapi.data.GetActiveContractsResponse;
import com.daml.ledger.javaapi.data.Identifier;
import com.daml.ledger.javaapi.data.InclusiveFilter;
import com.daml.ledger.javaapi.data.LedgerOffset;
import com.daml.ledger.javaapi.data.NoFilter;
import com.daml.ledger.javaapi.data.Transaction;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.javaapi.data.TransactionTree;
import com.daml.ledger.javaapi.data.codegen.Update;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.google.protobuf.Empty;
import com.rln.client.damlClient.commandId.RandomGenerator;
import com.rln.damlCodegen.da.internal.template.Archive;
import com.rln.damlCodegen.da.types.Tuple2;
import com.rln.damlCodegen.model.balance.Balance;
import com.rln.damlCodegen.model.balance.BalanceKey;
import com.rln.damlCodegen.workflow.initiatetransfer.InitiateTransfer;
import com.rln.damlCodegen.workflow.transactionmanifest.TransactionManifest;
import com.rln.damlCodegen.workflow.transferproposal.TransferProposal;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RLNDamlClient implements RLNClient {


    private static final Logger logger = LoggerFactory.getLogger(RLNClient.class);
    private static final RandomGenerator commandIdGenerator = new RandomGenerator();
    private static final String APP_ID = "RLN_APP";
    private static final String WORK_ID = "RLN";

    private final DamlLedgerClient ledger;
    private final PublishSubject<ClientCommand> commandPublisher;

    public RLNDamlClient(String host, int port, int numberOfLedgerSubmitterThreads,
        long ledgerBatchSubmissionMaxMsec, long ledgerBatchSubmissionMaxSize) {
        logger.info("Starting RLN daml client...");
        this.ledger = DamlLedgerClient.newBuilder(host, port).build();
        ledger.connect();
        logger.info("RLN Daml client connected to host {}, port {}", host, port);

        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(numberOfLedgerSubmitterThreads));
        this.commandPublisher = PublishSubject.create();
        commandPublisher
            .window(ledgerBatchSubmissionMaxMsec, TimeUnit.MILLISECONDS, ledgerBatchSubmissionMaxSize, true)
            .flatMapSingle(obs -> obs.toMultimap(cmd -> cmd.party))
            .flatMapIterable(Map::entrySet)
            .flatMapSingle(partyAndClientCommands ->
                Single.just(Empty.getDefaultInstance())
                    .flatMap(_empty -> sendCommands(partyAndClientCommands.getKey(), partyAndClientCommands.getValue()))
                    .subscribeOn(scheduler))
            .subscribe(Functions.emptyConsumer(), e -> logger.error(
                "Error while sending commands: %s%n", e));
    }

    private Single<Empty> sendCommands(String party, Collection<ClientCommand> clientCommands) {
        var commandId = commandIdGenerator.generate();
        var commands =  clientCommands.stream().map(clientCommand -> clientCommand.update).collect(Collectors.toList());

        logStartOfSubmission(commandId, clientCommands);
        return ledger
            .getCommandSubmissionClient()
            .submit(WORK_ID, APP_ID, commandId, party, commands)
            .doOnSuccess(logEndOfSubmission(commandId))
            .doOnError(errorHandler(commandId));
    }

    @Override
    public void createInitiateTransferContract(InitiateTransfer initiateTransfer) {
        var event = String.format("Create Initiate Transfer (%s, %s)", initiateTransfer.groupId, initiateTransfer.initiator);
        commandPublisher
            .onNext(new ClientCommand(event, initiateTransfer.create(), initiateTransfer.initiator,
              new InitiateTransferParameters(initiateTransfer)));
    }

  @Override
  public void createOrUpdateAutoApproveMarker(AutoApproveParameters autoApproveParameters) {
    autoApproveParameters.createOrUpdate().ifPresent(update ->
        commandPublisher.onNext(new ClientCommand(
            autoApproveParameters.event(),
            update,
            autoApproveParameters.getOwner(),
            autoApproveParameters)
        )
    );
  }

  @Override
    public void exerciseCreateProposalsChoice(String groupId, CreateProposalsChoiceParameters createProposalChoiceParameters) {
        var event = String.format("Exercise Create Proposal (%s, %s)", groupId, createProposalChoiceParameters.getInitiateTransferCid());
        Update exerciseCreateProposals = createProposalChoiceParameters.getInitiateTransferCid().exerciseCreateProposals(
            createProposalChoiceParameters.getMessageIdToLegs(), createProposalChoiceParameters.getAssemblerPartyId());
        commandPublisher.onNext(new ClientCommand(event, exerciseCreateProposals, createProposalChoiceParameters.getSchedulerPartyId(), createProposalChoiceParameters));
    }

    @Override
    public void exerciseApproveRejectProposalChoice(String groupId, ApproveRejectProposalChoiceParameters acceptRejectChoiceParameters) {
        var event = String.format("Exercise Approve Reject Proposal (%s, %s)", groupId, acceptRejectChoiceParameters.getContractId());
        TransferProposal.ContractId contractId = acceptRejectChoiceParameters.getContractId();
        final Update<?> exerciseUpdate;
        if (acceptRejectChoiceParameters.isApproved()) {
            exerciseUpdate = contractId.exerciseApproveProposal(Optional.ofNullable(acceptRejectChoiceParameters.getReason()), acceptRejectChoiceParameters.isSettleOnLedger());
        } else {
            exerciseUpdate = contractId.exerciseRejectProposal(Optional.ofNullable(acceptRejectChoiceParameters.getReason()));
        }
        commandPublisher.onNext(new ClientCommand(event, exerciseUpdate, acceptRejectChoiceParameters.getBankPartyId(), acceptRejectChoiceParameters));
    }

    @Override
    public void exerciseFinalizeRejectSettlement(String groupId, FinalizeRejectSettlementChoiceParameters finalizeRejectSettlementChoiceParameters) {
        var event = String.format("Exercise Finalize Reject Settlement (%s, %s)", groupId, finalizeRejectSettlementChoiceParameters.getTransactionManifestCid());
        TransactionManifest.ContractId contractId = finalizeRejectSettlementChoiceParameters.getTransactionManifestCid();
        final Update<?> exerciseUpdate;
        if (finalizeRejectSettlementChoiceParameters.isApproved()) {
            exerciseUpdate = contractId.exerciseFinalizeSettlement(Optional.ofNullable(finalizeRejectSettlementChoiceParameters.getReason()));
        } else {
            exerciseUpdate = contractId.exerciseRejectSettlement(Optional.ofNullable(finalizeRejectSettlementChoiceParameters.getReason()));
        }
        commandPublisher.onNext(new ClientCommand(event, exerciseUpdate, finalizeRejectSettlementChoiceParameters.getAssemblerPartyId(), finalizeRejectSettlementChoiceParameters));
    }

  @Override
    public Flowable<Transaction> getTransactions(LedgerOffset offset, TransactionFilter filter) {
        return ledger.getTransactionsClient().getTransactions(offset, filter, true);
    }

    @Override
    public Flowable<TransactionTree> getTransactionTrees(String subscriberParty, LedgerOffset offset) {
        return ledger.getTransactionsClient().getTransactionsTrees(offset, new FiltersByParty(
            Collections.singletonMap(subscriberParty, NoFilter.instance)), true);
    }

    @Override
    public Flowable<CreatedEvent> getActiveContracts(TransactionFilter filter) {
        return getActiveContractsResponse(filter).concatMapIterable(GetActiveContractsResponse::getCreatedEvents);
    }

  @Override
  public Flowable<GetActiveContractsResponse> getActiveContractsResponse(TransactionFilter filter) {
    return ledger.getActiveContractSetClient().getActiveContracts(filter, true);
  }

    @Override
    public void subscribeForContinuousEvent(String subscriberParty, Set<Identifier> templatesIncluded, Consumer<Event> consumer) {
        logger.info("Start subscribing for templates {} (party: {})", templatesIncluded, subscriberParty);
        var filter = new FiltersByParty(Map.of(subscriberParty, new InclusiveFilter(templatesIncluded, Map.of())));
        var activeContractsWithOffset = getActiveContractsAndOffset(filter);
        var contracts = activeContractsWithOffset._1;
        var offset = activeContractsWithOffset._2;

        //noinspection ResultOfMethodCallIgnored
        contracts.forEach(consumer);

        //noinspection ResultOfMethodCallIgnored
        getTransactions(offset, filter)
            .map(Transaction::getEvents)
            .flatMap(Flowable::fromIterable)
            .forEach(consumer);
    }

    @Override
    public void archiveBalance(ArchiveBalanceParameters parameters) {
        var event = String.format("Archive balance %s (provider: %s)", parameters.getIban(), parameters.getProvider());
        var update = Balance.byKey(new BalanceKey(parameters.getProvider(), parameters.getIban())).exerciseArchive(new Archive());
        commandPublisher.onNext(new ClientCommand(event, update, parameters.getProvider(), parameters));
    }

    @Override
    public void changeBalance(ChangeBalanceParameters parameters) {
        var event = String.format("Changing balance %s (provider: %s)", parameters.getIban(), parameters.getProvider());
        if (parameters.getChange().compareTo(BigDecimal.ZERO) == 0) {
          return;
        }
        Update<?> update;
        if (parameters.getChange().compareTo(BigDecimal.ZERO) < 0) {
          update = Balance.byKey(new BalanceKey(parameters.getProvider(), parameters.getIban())).exerciseDecrease(parameters.getChange().abs());
        } else {
          update = Balance.byKey(new BalanceKey(parameters.getProvider(), parameters.getIban())).exerciseIncrease(parameters.getChange().abs());
        }
        commandPublisher.onNext(new ClientCommand(event, update, parameters.getProvider(), parameters));
    }

  // helper functions
    private Tuple2<Flowable<CreatedEvent>, LedgerOffset> getActiveContractsAndOffset(TransactionFilter filter) {
        Flowable<GetActiveContractsResponse> responses = ledger.getActiveContractSetClient()
            .getActiveContracts(filter, true);

        var contracts = responses.map(GetActiveContractsResponse::getCreatedEvents)
            .flatMap(Flowable::fromIterable);

        LedgerOffset currentOffset = responses.blockingLast().getOffset()
            .map(offsetStr -> (LedgerOffset) new LedgerOffset.Absolute(offsetStr))
            .orElse(LedgerOffset.LedgerEnd.getInstance());

        return new Tuple2<>(contracts, currentOffset);
    }

    private void logStartOfSubmission(String commandId, Collection<ClientCommand> clientCommands) {
        for (var clientCommand : clientCommands) {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting submission of {} [{}], parameters: {}", clientCommand.event, commandId, clientCommand.parameters);
            } else {
                logger.info("Starting submission of {} [{}]", clientCommand.event, commandId);
            }
        }
    }

    private Consumer<Empty> logEndOfSubmission(String commandId) {
        return e -> logger.info("Finished submission [{}]", commandId);
    }

    private Consumer<? super Throwable> errorHandler(String commandId) {
        return error -> logger.error("Error [{}]: {}", commandId, error);
    }

    private static class ClientCommand {
        public final String event;
        public final Update<?> update;
        public final String party;
        public final Parameters parameters;

        ClientCommand(String event, Update<?> update, String party, Parameters parameters) {
            this.event = event;
            this.update = update;
            this.party = party;
            this.parameters = parameters;
        }
    }

    private static class InitiateTransferParameters implements Parameters {
        private final InitiateTransfer initiateTransfer;

        public InitiateTransferParameters(InitiateTransfer initiateTransfer) {
            this.initiateTransfer = initiateTransfer;
        }

        @Override
        public String toString() {
            return initiateTransfer.toString();
        }
    }
}
