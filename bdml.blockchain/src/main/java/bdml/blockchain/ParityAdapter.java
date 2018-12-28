package bdml.blockchain;

import bdml.blockchain.jsonrpc.JsonRpc;
import bdml.blockchain.parity.*;

import java.io.IOException;

public class ParityAdapter {
    private final String URI;

    public ParityAdapter(String uri) {
        this.URI = uri;
    }

    /**
     * Creates a new account using personal_newAccount.
     *
     * @param password Password for the new account.
     * @return The identifier of the new account.
     */
    public String createAccount(String password) {
        NewAccount request = new NewAccount(JsonRpc.getId(), password);
        NewAccountResponse response = JsonRpc.send(URI, request, NewAccountResponse.class);
        return response.result;
    }

    /**
     * Returns the information about a transaction requested by transaction hash.
     *
     * @param txHash 32 Bytes hash of a transaction
     * @return A transaction object or null when no transaction was found.
     */
    public Transaction getTransactionByHash(String txHash) {
        GetTransactionByHash request = new GetTransactionByHash(JsonRpc.getId(), txHash);
        GetTransactionByHashResponse response = JsonRpc.send(URI, request, GetTransactionByHashResponse.class);
        return response.result;
    }
}
