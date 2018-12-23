package bdml.blockchain;

import java.io.IOException;

import bdml.blockchain.jsonrpc.JsonRpc;
import bdml.blockchain.parity.*;
import bdml.services.Blockchain;

public class BlockchainAdapter implements Blockchain {
    private final String URI;

    public BlockchainAdapter() {
        // TODO: get blockchain URI from application.properties
        this.URI = "http://localhost:8545";
    }

    /**
     * Creates a new account using personal_newAccount.
     *
     * @return 20 Bytes identifier of the new account.
     */
    @Override
    public String createAccount(String password) {
        NewAccount request = new NewAccount(JsonRpc.getId(), password);
        try {
            NewAccountResponse response = JsonRpc.send(URI, request, NewAccountResponse.class);
            return response.result;
        } catch (IOException e) {
            // TODO: Error Handling: wrap exception
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns the information about a transaction requested by transaction hash.
     *
     * @param txHash 32 Bytes hash of a transaction
     * @return A transaction object or null when no transaction was found.
     */
    public Transaction getTransactionByHash(String txHash) {
        GetTransactionByHash request = new GetTransactionByHash(JsonRpc.getId(), txHash);

        GetTransactionByHashResponse response;
        try {
            response = JsonRpc.send(URI, request, GetTransactionByHashResponse.class);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // TODO: Error Handling: wrap exception
            throw new RuntimeException(e.getMessage());
        }

        if (response.result == null)
            return null;

        return response.result;
    }
}
