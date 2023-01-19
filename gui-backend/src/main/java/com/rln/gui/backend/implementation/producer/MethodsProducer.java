package com.rln.gui.backend.implementation.producer;

import com.rln.client.damlClient.RLNClient;
import com.rln.client.damlClient.partyManagement.PartyManager;
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
import com.rln.gui.backend.implementation.methods.PartyApiImpl;
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
  public PartyApiImpl getPartyApiImpl(
      GuiBackendConfiguration guiBackendConfiguration,
      PartyManager partyManager) {
    return new PartyApiImpl(guiBackendConfiguration, partyManager);
  }

  @Singleton
  @Produces
  public BalancesApiImpl getBalancesApiImpl(GuiBackendConfiguration guiBackendConfiguration,
      LiquidBalanceCache liquidBalanceCache,
      IncomingBalanceCache incomingBalanceCache,
      LockedBalanceCache lockedBalanceCache,
      AccountCache accountCache,
      RLNClient rlnClient) {
    return new BalancesApiImpl(guiBackendConfiguration, liquidBalanceCache, incomingBalanceCache, lockedBalanceCache, accountCache, rlnClient);
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
