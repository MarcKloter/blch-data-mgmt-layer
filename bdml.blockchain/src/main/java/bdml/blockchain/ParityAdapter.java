package bdml.blockchain;

import bdml.blockchain.web3j.EventStorage;
import bdml.blockchain.web3j.PersonalTransactionManager;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;

public class ParityAdapter {
    private Admin web3j;

    public ParityAdapter(String url) {
        this.web3j = Admin.build(new HttpService(url));
    }

    /**
     * Creates a new account using personal_newAccount.
     *
     * @param password Password for the new account.
     * @return The address of the new account.
     */
    public String createAccount(String password) {
        try {
            NewAccountIdentifier accountIdentifier = web3j.personalNewAccount(password).send();
            return accountIdentifier.getAccountId();
        } catch (IOException e) {
            // TODO: handle exception
            throw new RuntimeException();
        }
    }

    /**
     * Calls the newData method of the deployed smart contract using personal_sendTransaction.
     *
     * @param fromAddress the address to send the transaction from
     * @param password passphrase to unlock the fromAddress account
     * @param contractAddress the address the EventStorage contract is deployed at
     * @param identifier 20 bytes identifier as input for the newData method
     * @param payload bytes array payload as input for the newData method
     * @return The transaction hash, or the zero hash if the transaction is not yet available.
     */
    public String storeData(String fromAddress, String password, String contractAddress, BigInteger identifier, byte[] payload) {
        TransactionManager transactionManager = new PersonalTransactionManager(web3j, fromAddress, password);

        // TODO: GAS LIMIT has to be determined dynamically
        BigInteger GAS_PRICE = BigInteger.valueOf(0x0);
        BigInteger GAS_LIMIT = BigInteger.valueOf(0xfffff);

        ContractGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
        EventStorage contract = EventStorage.load(contractAddress, web3j, transactionManager, gasProvider);
        try {
            TransactionReceipt receipt = contract.newData(identifier, payload).send();
            // the receipt holds amongst other things: block hash, transaction index, transaction hash
            return receipt.getTransactionHash();
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException(e);
        }
    }
}
