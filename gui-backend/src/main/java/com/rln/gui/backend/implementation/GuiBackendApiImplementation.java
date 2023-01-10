package com.rln.gui.backend.implementation;
import com.rln.gui.backend.api.DefaultApi;
import com.rln.gui.backend.implementation.methods.AutoapproveApiImpl;
import com.rln.gui.backend.implementation.methods.BalancesApiImpl;
import com.rln.gui.backend.implementation.methods.TransactionsApiImpl;
import com.rln.gui.backend.model.Approval;
import com.rln.gui.backend.model.ApprovalProperties;
import com.rln.gui.backend.model.Balance;
import com.rln.gui.backend.model.BalanceChange;
import com.rln.gui.backend.model.ClientDTO;
import com.rln.gui.backend.model.Finalised;
import com.rln.gui.backend.model.LedgerAddressDTO;
import com.rln.gui.backend.model.PartyDTO;
import com.rln.gui.backend.model.Transaction;
import com.rln.gui.backend.model.TransactionStatusUpdate;
import com.rln.gui.backend.model.TransferProposal;
import com.rln.gui.backend.model.WalletAddressDTO;
import com.rln.gui.backend.model.WalletAddressTestDTO;
import com.rln.gui.backend.model.WalletDTO;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class GuiBackendApiImplementation implements DefaultApi {

  private final AutoapproveApiImpl autoApproveApi;
  private final TransactionsApiImpl transactionsApi;
  private final BalancesApiImpl balancesApi;

  public GuiBackendApiImplementation(
      AutoapproveApiImpl autoApproveApi,
      TransactionsApiImpl transactionsApi,
      BalancesApiImpl balancesApi) {

    this.autoApproveApi = autoApproveApi;
    this.transactionsApi = transactionsApi;
    this.balancesApi = balancesApi;
  }

  // Endpoints that we had in the previous API as well:

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

  @Override
  public List<Balance> getAddressBalance(String address) {
    return balancesApi.getAddressBalance(address);
  }

  // Endpoints that are new:
  // There is a `*` mark if there are any questions wrt the endpoint.

  // * what is the exact purpose of this endpoint?
  // Inform the participant of a decision on a group of messages
  @Override
  public void finalised(@Valid Finalised finalised) {
    throw notImplemented();
  }

  // Change a balance. This affects the 'hot', 'pessimistic', and 'actual' balances
  @Override
  public List<Balance> changeBalance(String address, @Valid @NotNull BalanceChange balanceChange) {
    throw notImplemented();
  }

  // * what is a ledger address?
  // Delete the specified ledger address. The address's balance must be zero.
  @Override
  public void delete(String address) {
    throw notImplemented();
  }

  // * what is the body?
  // Delete a ledger client
  @Override
  public void delete1(Long clientId, @Valid Long body) {
    throw notImplemented();
  }

  // * what is a wallet? how does it relate to addresses?
  // Delete an address from a wallet
  @Override
  public void delete2(Long walletId, String address) {
    throw notImplemented();
  }

  // Get all the known ledger addresses
  @Override
  public List<LedgerAddressDTO> get() {
    throw notImplemented();
  }

  // * what is a client?
  // Get all the clients
  @Override
  public List<ClientDTO> get1() {
    throw notImplemented();
  }

  // * what kind of party is this about?
  // Get all the known parties
  @Override
  public List<PartyDTO> get2() {
    throw notImplemented();
  }

  // * clarification about wallets and addresses
  // Get all the wallet addresses in the specified wallet
  @Override
  public List<WalletAddressDTO> get3(Long walletId) {
    throw notImplemented();
  }

  // * clarification about wallets and addresses
  // Get all the balances for all addresses in a wallet
  @Override
  public List<Balance> getBalances(Long walletId) {
    throw notImplemented();
  }

  // * What is a local address? (in our model) do we know whether something is local?
  // Get the balance for an address, provided the address is local to this participant
  @Override
  public List<Balance> getLocalBalance(String address) {
    throw notImplemented();
  }

  // Get the 'id'', 'name', and 'bic' of this party
  @Override
  public void getMyParty() {
    throw notImplemented();
  }

  // * clarification about wallets
  // Get all the wallets
  @Override
  public List<WalletDTO> getWallets() {
    throw notImplemented();
  }

  // Create a new ledger address
  @Override
  public Object post(@Valid @NotNull LedgerAddressDTO ledgerAddressDTO) {
    throw notImplemented();
  }

  // Create a new client. Returns the internal ID assigned to the new client.
  @Override
  public void post1(@Valid @NotNull ClientDTO clientDTO) {
    throw notImplemented();
  }

  // Create a new party
  @Override
  public void post2(@Valid @NotNull PartyDTO partyDTO) {
    throw notImplemented();
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

  // * why couldn't they?
  // Test if balances can be retrieved for a proposed wallet address
  @Override
  public Object testWalletAddress(String address,
      @Valid @NotNull WalletAddressTestDTO walletAddressTestDTO) {
    throw notImplemented();
  }

  private static WebApplicationException notImplemented() {
    return new WebApplicationException(Status.NOT_IMPLEMENTED);
  }
}
