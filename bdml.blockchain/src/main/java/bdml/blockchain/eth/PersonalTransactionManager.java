package bdml.blockchain.eth;

import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;

/**
 * TransactionManager to use with the personal module (https://wiki.parity.io/JSONRPC-personal-module).
 * Implemented as alternative to ClientTransactionManager that utilizes eth_sendTransaction.
 */
public class PersonalTransactionManager extends TransactionManager {

    private final Admin admin;
    private final String password;

    public PersonalTransactionManager(Admin admin, String fromAddress, String password) {
        super(admin, fromAddress);
        this.admin = admin;
        this.password = password;
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice,
                                              BigInteger gasLimit,
                                              String to,
                                              String data,
                                              BigInteger value) throws IOException {
        Transaction transaction = new Transaction(getFromAddress(), null, gasPrice, gasLimit, to, value, data);

        return admin.personalSendTransaction(transaction, password).send();
    }
}
