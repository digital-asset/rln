package com.rln.gui.backend.jobs;

import com.daml.ledger.javaapi.data.Event;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.rln.client.damlClient.RLNClient;
import com.rln.messageprocessing.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CacheInitializationJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CacheInitializationJob.class);
    private final RLNClient rlnClient;
    private final TransactionFilter filter;
    private final MessageProcessor<Event> eventProcessor;

    public CacheInitializationJob(RLNClient rlnClient,
                                  TransactionFilter filter,
                                  MessageProcessor<Event> eventProcessor) {
        this.rlnClient = rlnClient;
        this.filter = filter;
        this.eventProcessor = eventProcessor;
    }

    @Override
    public void run() {
        logger.info("Initializing cache...");
        rlnClient.getActiveContracts(filter)
                .forEach(eventProcessor::updateCache)
                .dispose();
        logger.info("Cache initialization completed.");
    }
}
