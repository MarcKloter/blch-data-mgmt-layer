package bdml.blockchain.eth;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.web3j.crypto.*;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.exceptions.TxHashMismatchException;
import org.web3j.utils.Numeric;
import org.web3j.utils.TxHashVerifier;

/**
 * TransactionManager implementation using Ethereum wallet file to create and sign transactions
 * locally.
 *
 * <p>This transaction manager provides support for specifying the chain id for transactions as per
 * <a href="https://github.com/ethereum/EIPs/issues/155">EIP155</a>, as well as for locally signing
 * RawTransaction instances without broadcasting them.
 */
public class AnonymousTransactionManager extends TransactionManager {

    private final Admin web3j;
    final Credentials credentials;


    protected TxHashVerifier txHashVerifier = new TxHashVerifier();

    private AnonymousTransactionManager(Admin web3j, Credentials credentials) {
        super(web3j, credentials.getAddress());
        this.web3j = web3j;
        this.credentials = credentials;
    }

    public AnonymousTransactionManager(Admin web3j) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        this(web3j, Credentials.create(Keys.createEcKeyPair()));
    }

    @Override
    public EthSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, BigInteger value) throws IOException {

        BigInteger nonce = BigInteger.ZERO;
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                value,
                data);

        return signAndSend(rawTransaction);
    }

    /*
     * @param rawTransaction a RawTransaction istance to be signed
     * @return The transaction signed and encoded without ever broadcasting it
     */
    private String sign(RawTransaction rawTransaction) {
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        return Numeric.toHexString(signedMessage);
    }

    private EthSendTransaction signAndSend(RawTransaction rawTransaction)
            throws IOException {
        String hexValue = sign(rawTransaction);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction != null && !ethSendTransaction.hasError()) {
            String txHashLocal = Hash.sha3(hexValue);
            String txHashRemote = ethSendTransaction.getTransactionHash();
            if (!txHashVerifier.verify(txHashLocal, txHashRemote)) {
                throw new TxHashMismatchException(txHashLocal, txHashRemote);
            }
        }

        return ethSendTransaction;
    }
}