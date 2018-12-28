package bdml.blockchain;

import java.io.IOException;

import bdml.blockchain.jsonrpc.JsonRpc;
import bdml.blockchain.parity.*;
import bdml.blockchain.persistence.AccountMap;
import bdml.services.Blockchain;
import bdml.services.api.types.Account;
import bdml.services.types.StorageObject;

/**
 * The BlockchainFacade implements the Blockchain interface.
 */
public class BlockchainFacade implements Blockchain {
    private final AccountMap accounts;

    private ParityAdapter blockchain;

    public BlockchainFacade() {
        // TODO: get blockchain URI from application.properties
        String URI = "http://localhost:8545";
        this.accounts = new AccountMap();
        this.blockchain = new ParityAdapter(URI);
    }

    @Override
    public void createEntity(Account account) {
        String address = blockchain.createAccount(account.getPassword());

        // store the address associated to the given id
        accounts.put(account.getIdentifier(), address);
    }

    @Override
    public void createTransaction(Account account, StorageObject payload) {

    }

    @Override
    public StorageObject getTransaction(byte[] identifier) {
        return null;
    }
}
