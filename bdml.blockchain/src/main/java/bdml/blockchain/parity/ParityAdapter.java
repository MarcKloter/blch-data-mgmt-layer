package bdml.blockchain.parity;

import bdml.blockchain.web3j.EventStorage;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class ParityAdapter {
    private final String HEX_PREFIX = "0x";
    private Admin web3j;
    private final String EVENT_TOPIC = EventEncoder.encode(EventStorage.DATAEVENT_EVENT);
    private final List<String> CONTRACT_ADDRESS;

    public ParityAdapter(String url, String contractAddress) {
        this.web3j = Admin.build(new HttpService(url));
        this.CONTRACT_ADDRESS = Collections.singletonList(contractAddress);
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
     * @param identifier 20 bytes identifier as input for the newData method
     * @param frame bytes array containing the frame as input for the newData method
     * @return The transaction hash, or the zero hash if the transaction is not yet available.
     */
    public String storeData(String fromAddress, String password, byte[] identifier, byte[] frame) {
        TransactionManager transactionManager = new PersonalTransactionManager(web3j, fromAddress, password);

        // TODO: GAS LIMIT has to be determined dynamically
        // TODO: take a look at https://github.com/web3j/web3j/blob/master/integration-tests/src/test/java/org/web3j/protocol/scenarios/EventFilterIT.java
        BigInteger GAS_PRICE = BigInteger.valueOf(0x0);
        BigInteger GAS_LIMIT = BigInteger.valueOf(0xfffff);

        ContractGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
        EventStorage contract = EventStorage.load(CONTRACT_ADDRESS.get(0), web3j, transactionManager, gasProvider);
        try {
            TransactionReceipt receipt = contract.newData(new BigInteger(Hex.encodeHexString(identifier), 16), frame).send();
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
     * @return Byte array containing the frame within the event log matching the given {@code identifier} or null.
     */
    public byte[] getFrame(byte[] identifier) {
        Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");

        List<EthLog.LogResult> results = getLogs(identifier, null, null, null);

        // proof of concept implementation: take the oldest event matching the identifier
        return results.stream().findFirst().map(this::retrieveFrame).orElse(null);
    }

    public Set<Map.Entry<byte[], byte[]>> getAllFrames(byte[] fromIdentifier) {
        // first we need to retrieve the block number of the log containing the frame identified by fromIdentifier
        List<EthLog.LogResult> identifiedLogs = getLogs(fromIdentifier, null, null, null);

        // return null if the identifier does not exist
        if(identifiedLogs.isEmpty())
            return null;

        // the results are chronologically ordered (oldest first)
        EthLog.LogObject log = castToLogObject(identifiedLogs.get(0));
        BigInteger blockNumber = log.getBlockNumber();

        List<EthLog.LogResult> allLogsNewerThanBlockNumber = getLogs(null, blockNumber, null, null);

        return allLogsNewerThanBlockNumber.stream()
                .map(this::castToLogObject)
                .filter(logObject -> !logObject.getBlockNumber().equals(blockNumber)) // remove all logs of the fromBlock
                .map(this::toIdentifierFramePair)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map.Entry<byte[], byte[]> toIdentifierFramePair(EthLog.LogObject logObject) {
        byte[] idBytes;
        try {
            String identifier = logObject.getTopics().get(1);
            identifier = identifier.replaceFirst(HEX_PREFIX, "");
            idBytes = Hex.decodeHex(identifier);
        } catch (DecoderException e) {
            return null;
        }
        byte[] frame = retrieveFrame(logObject);
        return new AbstractMap.SimpleImmutableEntry<>(idBytes, frame);
    }

    public byte[] getLatestIdentifier() {
        // limit the result to 1, which will be the latest
        List<EthLog.LogResult> results = getLogs(null, null, null, 1);

        if(results.isEmpty())
            return null;

        EventValues log = parseLogResult(results.get(0));

        // the identifier is the only explicitly indexed value (event topic is implicit)
        BigInteger identifier = (BigInteger) log.getIndexedValues().get(0).getValue();
        return identifier.toByteArray();
    }

    private List<EthLog.LogResult> getLogs( byte[] identifier, BigInteger fromBlock, BigInteger toBlock, Integer limit) {
        DefaultBlockParameter from = (fromBlock != null) ? new DefaultBlockParameterNumber(fromBlock) : DefaultBlockParameterName.EARLIEST;
        DefaultBlockParameter to = (toBlock != null) ? new DefaultBlockParameterNumber(toBlock) : DefaultBlockParameterName.LATEST;

        EthFilter filter = (limit != null) ? new ParityFilter(from, to, CONTRACT_ADDRESS, limit) : new EthFilter(from, to, CONTRACT_ADDRESS);

        // filter for the event topic
        filter.addSingleTopic(EVENT_TOPIC);

        // filter for the identifier (parity requires hex prefix)
        String idTopic = (identifier != null) ? HEX_PREFIX + Hex.encodeHexString(identifier) : null;
        filter.addSingleTopic(idTopic);

        // get all logs matching the identifier
        return ethGetLogs(filter);
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

    private byte[] retrieveFrame(EthLog.LogResult logResult) {
        EventValues eventValues = parseLogResult(logResult);

        // return the frame (only nonIndexedValue)
        return (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
    }

    private EthLog.LogObject castToLogObject(EthLog.LogResult logResult) {
        if(logResult instanceof EthLog.LogObject) {
            return (EthLog.LogObject) logResult.get();
        } else {
            throw new RuntimeException("Unexpected result type: " + logResult.get().getClass() + " required LogObject");
        }
    }

    private EventValues parseLogResult(EthLog.LogResult logResult) {
        EthLog.LogObject log = castToLogObject(logResult);
        return Contract.staticExtractEventParameters(EventStorage.DATAEVENT_EVENT, log);
    }
}
