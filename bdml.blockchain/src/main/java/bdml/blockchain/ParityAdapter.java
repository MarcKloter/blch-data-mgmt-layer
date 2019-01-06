package bdml.blockchain;

import bdml.blockchain.web3j.EventStorage;
import bdml.blockchain.web3j.PersonalTransactionManager;
import org.apache.commons.codec.binary.Hex;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.filters.LogFilter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class ParityAdapter {
    private final String HEX_PREFIX = "0x";
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
    public String storeData(String fromAddress, String password, String contractAddress, byte[] identifier, byte[] payload) {
        TransactionManager transactionManager = new PersonalTransactionManager(web3j, fromAddress, password);

        // TODO: GAS LIMIT has to be determined dynamically
        // TODO: take a look at https://github.com/web3j/web3j/blob/master/integration-tests/src/test/java/org/web3j/protocol/scenarios/EventFilterIT.java
        BigInteger GAS_PRICE = BigInteger.valueOf(0x0);
        BigInteger GAS_LIMIT = BigInteger.valueOf(0xfffff);

        ContractGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
        EventStorage contract = EventStorage.load(contractAddress, web3j, transactionManager, gasProvider);
        try {
            TransactionReceipt receipt = contract.newData(new BigInteger(Hex.encodeHexString(identifier), 16), payload).send();
            // the receipt holds amongst other things: block hash, transaction index, transaction hash
            return receipt.getTransactionHash();
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Queries event logs for an indexed identifier using eth_getLogs.
     *
     * @param identifier 32 bytes indexed event identifier
     * @return List of the frames contained within the logs matching the given {@code identifier} (might be empty).
     */
    public List<byte[]> getLogs(byte[] identifier) {
        Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");

        // passing null to the address parameter for jackson to Include.NON_NULL
        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, (List<String>) null);
        // get event topic
        String eventTopic = EventEncoder.encode(EventStorage.DATAEVENT_EVENT);
        // filter for event topic
        filter.addSingleTopic(eventTopic);
        // filter for identifier (parity requires hex prefix)
        filter.addSingleTopic(HEX_PREFIX + Hex.encodeHexString(identifier));

        // get all logs matching the identifier
        List<EthLog.LogResult> logResults = ethGetLogs(filter);

        return logResults.stream().map(this::getPayload).collect(Collectors.toList());
    }

    public List<byte[]> getAllLogs() {
        return getAllLogs(null);
    }

    public List<byte[]> getAllLogs(String fromAddress) {
        List<String> address = (fromAddress != null) ? Collections.singletonList(fromAddress) : null;
        // passing null to the address parameter for jackson to Include.NON_NULL
        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, address);
        // get event topic
        String eventTopic = EventEncoder.encode(EventStorage.DATAEVENT_EVENT);
        // filter for event topic
        filter.addSingleTopic(eventTopic);

        List<EthLog.LogResult> logResults = ethGetLogs(filter);

        return logResults.stream().map(this::getPayload).collect(Collectors.toList());
    }

    private List<EthLog.LogResult> ethGetLogs(EthFilter filter) {
        try {
            // use eth_getLogs to retrieve list of logs, return the first
            return web3j.ethGetLogs(filter).send().getLogs();
        } catch (IOException e) {
            // TODO: handle exception
            throw new RuntimeException(e);
        }
    }

    private byte[] getPayload(EthLog.LogResult logResult) {
        if(logResult instanceof EthLog.LogObject) {
            // unwrap log
            Log log = (EthLog.LogObject) logResult.get();
            // parse log data
            EventValues eventValues = Contract.staticExtractEventParameters(EventStorage.DATAEVENT_EVENT, log);
            // get the payload (only nonIndexedValue)
            return (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        } else {
            // TODO: handle exception
            throw new RuntimeException("Unexpected result type: " + logResult.get().getClass() + " required LogObject");
        }
    }
}
