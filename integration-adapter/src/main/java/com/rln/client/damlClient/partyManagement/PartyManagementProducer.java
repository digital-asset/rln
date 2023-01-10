/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.damlClient.partyManagement;

import com.rln.client.damlClient.RLNClient;
import com.rln.common.IAConstants;
import com.rln.configuration.AdapterConfiguration;
import com.rln.configuration.AdapterMode;
import com.rln.configuration.DamlLedgerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileNotFoundException;

public class PartyManagementProducer {

    Logger logger = LoggerFactory.getLogger(PartyManagementProducer.class);

    @Singleton
    @Produces
    public BicPartyIdMapper getBicPartyIdMapper(RLNClient rlnClient, DamlLedgerConfiguration damlLedgerConfiguration) {
        logger.info("Created BicPartyIdMapper with RLNClient {}", rlnClient);
        return new BicPartyIdMapper(rlnClient, damlLedgerConfiguration.bankBicReadingPartyId());
    }

    @Singleton
    @Produces
    public PartyManager getDamlBankBicPartyManager(BicPartyIdMapper mapper) {
        logger.info("Created DamlBankBicPartyManager with mapper {}", mapper);
        return new DamlBankBicPartyManager(mapper);
    }


    @ApplicationScoped
    public RandomShardPartyPicker getRandomShardPartyPicker(
            AdapterConfiguration adapterConfiguration,
            @Named(IAConstants.ASSEMBLER_SHARD_PARTY_READER)
                    ShardPartyPlainTextListReader assemblerShardPartyPlainTextListReader,
            @Named(IAConstants.SCHEDULER_SHARD_PARTY_READER)
                    ShardPartyPlainTextListReader schedulerShardPartyPlainTextListReader) {
        if (adapterConfiguration.mode().equals(AdapterMode.BANK)) {
            return new RandomShardPartyPicker(schedulerShardPartyPlainTextListReader);
        } else if (adapterConfiguration.mode().equals(AdapterMode.SCHEDULER)) {
            return new RandomShardPartyPicker(assemblerShardPartyPlainTextListReader);
        }
        return null;
    }

    @ApplicationScoped
    @Named(IAConstants.ASSEMBLER_SHARD_PARTY_READER)
    public ShardPartyPlainTextListReader getAssemblerShardPartyReader(
            AdapterConfiguration adapterConfiguration) throws FileNotFoundException {
        if (adapterConfiguration.mode().equals(AdapterMode.ASSEMBLER) || adapterConfiguration.mode().equals(AdapterMode.SCHEDULER)) {
            return ShardPartyPlainTextListReader.initializeFromPath(adapterConfiguration.assemblerShardPartiesConfig());
        }
        return null;
    }

    @ApplicationScoped
    @Named(IAConstants.SCHEDULER_SHARD_PARTY_READER)
    public ShardPartyPlainTextListReader getSchedulerShardPartyReader(
            AdapterConfiguration adapterConfiguration) throws FileNotFoundException {
        if (adapterConfiguration.mode().equals(AdapterMode.BANK) || adapterConfiguration.mode().equals(AdapterMode.SCHEDULER)) {
            return ShardPartyPlainTextListReader.initializeFromPath(adapterConfiguration.schedulerShardPartiesConfig());
        }
        return null;
    }

    @ApplicationScoped
    @Named(IAConstants.BANK_SHARD_PARTY_READER)
    public ShardPartyPlainTextListReader getBankShardPartyReader(
            AdapterConfiguration adapterConfiguration) throws FileNotFoundException {
        if (adapterConfiguration.mode().equals(AdapterMode.BANK)) {
            return ShardPartyPlainTextListReader.initializeFromPath(adapterConfiguration.bankShardPartiesConfig());
        }
        return null;
    }
}
