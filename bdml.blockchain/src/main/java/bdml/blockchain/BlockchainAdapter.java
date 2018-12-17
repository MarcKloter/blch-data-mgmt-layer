package bdml.blockchain;

import java.io.IOException;

import bdml.blockchain.jsonrpc.JsonRpc;
import bdml.blockchain.parity.*;
import bdml.services.Blockchain;

public class BlockchainAdapter implements Blockchain {
    private String uri;

    public BlockchainAdapter() {
        // mandatory non-arg constructor for the JPMS
    }

    /**
     * Creates a new blockchain adpter to communicate with the connected blockchain.
     *
     * @param uri JSON-RPC API endpoint URI
     */
    public BlockchainAdapter(String uri) {
        this.uri = uri;
    }

    /**
     * Creates a new account using personal_newAccount.
     *
     * @param password password for the new account
     * @return 20 Bytes identifier of the new account.
     */
    public String createAccount(String password) {
        NewAccount request = new NewAccount(JsonRpc.getId(), password);
        try {
            NewAccountResponse response = JsonRpc.send(uri, request, NewAccountResponse.class);
            return response.result;
        } catch (IOException e) {
            // TODO: Error Handling: wrap exception
            throw new RuntimeException();
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
            response = JsonRpc.send(uri, request, GetTransactionByHashResponse.class);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // TODO: Error Handling: wrap exception
            throw new RuntimeException();
        }

        if (response.result == null)
            return null;

        return response.result;
    }
}
