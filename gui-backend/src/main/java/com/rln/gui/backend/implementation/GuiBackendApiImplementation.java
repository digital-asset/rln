package com.rln.gui.backend.implementation;

import com.rln.gui.backend.api.DefaultApi;
import com.rln.gui.backend.implementation.balanceManagement.data.AccountBalance;
import com.rln.gui.backend.implementation.balanceManagement.data.AccountInfo;
import com.rln.gui.backend.implementation.balanceManagement.data.BalanceType;
import com.rln.gui.backend.implementation.balanceManagement.exception.IbanNotFoundException;
import com.rln.gui.backend.implementation.balanceManagement.exception.NonZeroBalanceException;
import com.rln.gui.backend.implementation.config.GuiBackendConfiguration;
import com.rln.gui.backend.implementation.methods.AutoapproveApiImpl;
import com.rln.gui.backend.implementation.methods.BalancesApiImpl;
import com.rln.gui.backend.implementation.methods.InternalServerError;
import com.rln.gui.backend.implementation.methods.PartyApiImpl;
import com.rln.gui.backend.implementation.methods.TransactionsApiImpl;
import com.rln.gui.backend.model.Approval;
import com.rln.gui.backend.model.ApprovalProperties;
import com.rln.gui.backend.model.Balance;
import com.rln.gui.backend.model.BalanceChange;
import com.rln.gui.backend.model.ClientDTO;
import com.rln.gui.backend.model.Finalised;
import com.rln.gui.backend.model.LedgerAddressDTO;
import com.rln.gui.backend.model.MessageGroup;
import com.rln.gui.backend.model.PartyDTO;
import com.rln.gui.backend.model.Transaction;
import com.rln.gui.backend.model.TransactionStatusUpdate;
import com.rln.gui.backend.model.TransferProposal;
import com.rln.gui.backend.model.WalletAddressDTO;
import com.rln.gui.backend.model.WalletAddressTestDTO;
import com.rln.gui.backend.model.WalletDTO;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiBackendApiImplementation implements DefaultApi {

    public static final Long ONLY_SUPPORTED_WALLET_ID = 1L;
    public static final WalletDTO ONLY_SUPPORTED_WALLET = WalletDTO.builder()
            .data("Wallet 1")
            .id(ONLY_SUPPORTED_WALLET_ID)
            .build();
    private final Logger logger = LoggerFactory.getLogger(GuiBackendApiImplementation.class);

    private final GuiBackendConfiguration configuration;
    private final AutoapproveApiImpl autoApproveApi;
    private final TransactionsApiImpl transactionsApi;
    private final BalancesApiImpl balancesApi;
    private final PartyApiImpl partyApi;

    public GuiBackendApiImplementation(
            GuiBackendConfiguration configuration,
            AutoapproveApiImpl autoApproveApi,
            TransactionsApiImpl transactionsApi,
            BalancesApiImpl balancesApi,
            PartyApiImpl partyApi) {
        this.configuration = configuration;
        this.autoApproveApi = autoApproveApi;
        this.transactionsApi = transactionsApi;
        this.balancesApi = balancesApi;
        this.partyApi = partyApi;
    }

    @Override
    public List<Approval> transferProposal(@Valid TransferProposal transferProposal) {
        return transactionsApi.transferProposal(transferProposal);
    }

    @Override
    public Object updateTransactionApprovalStatus(
            @Valid TransactionStatusUpdate transactionStatusUpdate) {
        return transactionsApi.updateTransactionApprovalStatus(transactionStatusUpdate);
    }

    @Override
    public List<Transaction> getRequiredApprovals() {
        return transactionsApi.getRequiredApprovals();
    }

    @Override
    public void updateApprovalProperties(@Valid ApprovalProperties approvalProperties) {
        autoApproveApi.updateApprovalProperties(approvalProperties);
    }

    @Override
    public List<Transaction> getTransactions(Boolean incompleteOnly, String address, Long limit,
                                             Long offset) {
        return transactionsApi.getTransactions(incompleteOnly, address, limit, offset);
    }

    // Endpoint part of Minimum Necessary Requirements of the GUI
    @Override
    public List<Balance> getAddressBalance(String address) {
        try {
            return accountBalanceToBalances(balancesApi.getAddressBalance(address));
        } catch (IbanNotFoundException e) {
            throw notFound();
        }
    }


    // Change a balance. This affects the 'hot', 'pessimistic', and 'actual' balances
    @Override
    public List<Balance> changeBalance(String address, @Valid @NotNull BalanceChange balanceChange) {
        return accountBalanceToBalances(balancesApi.changeBalance(configuration.partyDamlId(), address, balanceChange));
    }

    @Override
    public void create(MessageGroup messageGroup) {
        throw notImplemented();
    }

    // * what is a ledger address?
    // Delete the specified ledger address. The address's balance must be zero.
    @Override
    public void delete(String address) {
        try {
            // Provider is always the party the GUI backend is acting in the name of
            balancesApi.delete(configuration.partyDamlId(), address);
        } catch (IbanNotFoundException e) {
            throw notFound();
        } catch (NonZeroBalanceException e) {
            throw forbidden();
        }
    }

    // Get all the known ledger addresses
    // This endpoint includes addresses where the current party is provider or owner
    @Override
    public List<LedgerAddressDTO> get() {
        return autoApproveApi.get();
    }

    // * What is a local address? (in our model) do we know whether something is local?
    // Get the balance for an address, provided the address is local to this participant
    @Override
    public List<Balance> getLocalBalance(String address) {
        try {
            return accountBalanceToBalances(balancesApi.getLocalBalance(address));
        } catch (IbanNotFoundException e) {
            throw notFound();
        }
    }

    // Create a new ledger address
    @Override
    public Object post(@Valid @NotNull LedgerAddressDTO ledgerAddressDTO) {
        throw notImplemented();
    }

    // Wallet management ----------

    // Endpoint part of Minimum Necessary Requirements of the GUI
    // Get all the wallets
    @Override
    public List<WalletDTO> getWallets() {
        return List.of(ONLY_SUPPORTED_WALLET);
    }

    // Create a new wallet address
    @Override
    public void post3(Long walletId, @Valid @NotNull WalletAddressDTO walletAddressDTO) {
        throw notImplemented();
    }

    // Create a new wallet
    @Override
    public void postWallets(@Valid @NotNull WalletDTO walletDTO) {
        throw notImplemented();
    }

    // Get all the balances for all addresses in a wallet
    @Override
    public List<Balance> getBalances(Long walletId) {
        if (!ONLY_SUPPORTED_WALLET_ID.equals(walletId)) {
            throw notFound();
        }
        List<Balance> result = new LinkedList<>();
        result.addAll(balancesApi.getRemoteBalances());
        result.addAll(balancesApi
                .getBalances(walletId)
                .stream()
                .map(this::accountBalanceToBalances)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
        return result;
    }

    // Get all the wallet addresses in the specified wallet
    @Override
    public List<WalletAddressDTO> get3(Long walletId) {
        if (!ONLY_SUPPORTED_WALLET_ID.equals(walletId)) {
            throw notFound();
        }
        return autoApproveApi.getWalletAddresses();
    }

    // Delete an address from a wallet
    @Override
    public void delete2(Long walletId, String address) {
        throw notImplemented();
    }

    // Client management ----------

    // Create a new client. Returns the internal ID assigned to the new client.
    @Override
    public void post1(@Valid @NotNull ClientDTO clientDTO) {
        throw notImplemented();
    }

    // Get all the clients
    @Override
    public List<ClientDTO> get1() {
        return partyApi.getClients();
    }

    // * what is the body?
    // Delete a ledger client
    @Override
    public void delete1(Long clientId) {
        throw notImplemented();
    }

    // Party management ----------

    // Get the 'id'', 'name', and 'bic' of this party
    @Override
    public PartyDTO getMyParty() {
        return partyApi.getMyParty();
    }

    // Create a new party
    @Override
    public void post2(@Valid @NotNull PartyDTO partyDTO) {
        throw notImplemented();
    }

    // Get all the known parties
    @Override
    public List<PartyDTO> get2() {
        try {
            return partyApi.getParties();
        } catch (InternalServerError e) {
            logger.error("Unable to list known parties", e);
            throw internalServerError();
        }
    }

    // Test endpoints (won't implement) -----

    // Test if balances can be retrieved for a proposed wallet address
    @Override
    public Object testWalletAddress(String address,
                                    @Valid @NotNull WalletAddressTestDTO walletAddressTestDTO) {
        throw notImplemented();
    }

    // Inform the participant of a decision on a group of messages
    @Override
    public void finalised(@Valid Finalised finalised) {
        throw notImplemented();
    }

    // -------------

    private List<Balance> accountBalanceToBalances(AccountBalance accountBalance) {
        var balance = Balance.builder()
                .address(accountBalance.getAddress())
                .assetId(AccountBalance.ASSET_ID) // default to 0 now, as we don't have assetId in the system yet
                .assetName(accountBalance.getAssetName())
                .assetOrLiability(getAssetOrLiabilityEnum(accountBalance.getAccountInfo()))
                .party(accountBalance.getProviderName())
                .client(accountBalance.getOwnerName());

        var pessimistic = balance.type(BalanceType.PESSIMISTIC.name())
                .balance(accountBalance.getLiquid())
                .build();
        var actual = balance.type(BalanceType.ACTUAL.name())
                .balance(accountBalance.getLiquid().add(accountBalance.getLocked()))
                .build();
        var hot = balance.type(BalanceType.HOT.name())
                .balance(accountBalance.getLiquid().add(accountBalance.getIncoming()))
                .build();

        return List.of(pessimistic, actual, hot);
    }

    private Balance.AssetOrLiabilityEnum getAssetOrLiabilityEnum(AccountInfo accountInfo) {
        return accountInfo.isLiabilityOf(configuration.partyDamlId())
          ? Balance.AssetOrLiabilityEnum.LIABILITY
          : Balance.AssetOrLiabilityEnum.ASSET;
    }

    private static WebApplicationException notImplemented() {
        return new WebApplicationException(Status.NOT_IMPLEMENTED);
    }

    private static WebApplicationException notFound() {
        return new WebApplicationException(Status.NOT_FOUND);
    }

    private static WebApplicationException forbidden() {
        return new WebApplicationException(Status.FORBIDDEN);
    }

    private static WebApplicationException internalServerError() {
        return new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
}
