package com.rln.gui.backend.implementation.producer;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.damlClient.partyManagement.RandomShardPartyPicker;
import com.rln.gui.backend.implementation.balanceManagement.cache.AccountCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.AutoApproveCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.IncomingBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LiquidBalanceCache;
import com.rln.gui.backend.implementation.balanceManagement.cache.LockedBalanceCache;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.implementation.converter.TransferProposalToApiTypeConverter;
import com.rln.gui.backend.implementation.methods.AutoapproveApiImpl;
import com.rln.gui.backend.implementation.methods.BalancesApiImpl;
import com.rln.gui.backend.implementation.methods.TransactionsApiImpl;
import com.rln.gui.backend.ods.TransferProposalRepository;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodsProducer {
  Logger logger = LoggerFactory.getLogger(DamlClientProducer.class);

  @Singleton
  @Produces
  public AutoapproveApiImpl getAutoapproveApiImpl(GuiBackendConfiguration guiBackendConfiguration, RLNClient rlnClient, AutoApproveCache autoApproveCache, AccountCache accountCache) {
    return new AutoapproveApiImpl(guiBackendConfiguration, autoApproveCache, accountCache, rlnClient);
  }

  @Singleton
  @Produces
  public BalancesApiImpl getBalancesApiImpl(LiquidBalanceCache liquidBalanceCache,
      IncomingBalanceCache incomingBalanceCache,
      LockedBalanceCache lockedBalanceCache,
      AccountCache accountCache,
      RLNClient rlnClient) {
    return new BalancesApiImpl(liquidBalanceCache, incomingBalanceCache, lockedBalanceCache, accountCache, rlnClient);
  }

  @Singleton
  @Produces
  public TransactionsApiImpl getTransactionsApiImpl(
      RandomShardPartyPicker schedulerRandomShardPartyPicker,
      GuiBackendConfiguration guiBackendConfiguration,
      TransferProposalToApiTypeConverter converter,
      TransferProposalRepository transferProposals,
      RLNClient rlnClient) {
    return new TransactionsApiImpl(schedulerRandomShardPartyPicker,
        guiBackendConfiguration,
        converter,
        transferProposals,
        rlnClient);
  }
}
