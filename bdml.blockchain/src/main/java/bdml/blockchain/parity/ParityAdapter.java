package bdml.blockchain.parity;

import bdml.blockchain.web3j.EventStorage;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.helper.FrameListener;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.protocol.websocket.events.Log;
import org.web3j.protocol.websocket.events.LogNotification;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ParityAdapter {
    private static final String HEX_PREFIX = "0x";
    private final String EVENT_TOPIC = EventEncoder.encode(EventStorage.DATAEVENT_EVENT);
    private final List<String> CONTRACT_ADDRESS;

    private Admin web3j;
    private Disposable eventSubscription;

    public ParityAdapter(String url, String contractAddress) {
        this.web3j = Admin.build(new HttpService(url));
        this.CONTRACT_ADDRESS = Collections.singletonList(contractAddress);
        checkContract();
    }

    /**
     * Checks whether every address in the CONTRACT_ADDRESS list is a deployed smart contract using eth_getCode.
     * https://wiki.parity.io/JSONRPC-eth-module#eth_getcode
     *
     * @throws MisconfigurationException if one of the addresses does not contain contract code.
     */
    private void checkContract() {
        for(String contractAddress : CONTRACT_ADDRESS) {
            EthGetCode contract;
            try {
                contract = web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send();
            } catch (IOException e) {
                throw new MisconfigurationException(e.getMessage());
            }

            if(contract.getCode().equals("0x"))
                throw new MisconfigurationException(String.format("The configured address: '%s' does not correspond to a smart contract.", contractAddress));
        }
    }

    /**
     * Creates a new account using personal_newAccount.
     * https://wiki.parity.io/JSONRPC-personal-module#personal_newaccount
     *
     * @param password Password for the new account.
     * @return The address of the new account.
     */
    public String createAccount(String password) {
        try {
            NewAccountIdentifier accountIdentifier = web3j.personalNewAccount(password).send();
            return accountIdentifier.getAccountId();
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Calls the newData method of the deployed smart contract using personal_sendTransaction.
     * https://wiki.parity.io/JSONRPC-personal-module#personal_sendtransaction
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
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Queries event logs for an indexed identifier using eth_getLogs.
     * https://wiki.parity.io/JSONRPC-eth-module#eth_getlogs
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

    /**
     * Queries all event logs newer than {@code fromBlock} using eth_getLogs.
     * https://wiki.parity.io/JSONRPC-eth-module#eth_getlogs
     *
     * @param fromBlock block number to start receiving frames after
     * @return Ordered set containing all identifier/frame pairs stored after the {@code fromBlock} in chronological
     * order (oldest first).
     */
    public LinkedHashSet<Map.Entry<byte[], byte[]>> getAllFrames(BigInteger fromBlock) {
        List<EthLog.LogResult> allLogsNewerThanBlockNumber = getLogs(null, fromBlock, null, null);

        LinkedHashSet<Map.Entry<byte[], byte[]>> result = new LinkedHashSet<>();
        allLogsNewerThanBlockNumber.stream()
                .map(this::castToLogObject)
                .filter(logObject -> !logObject.getBlockNumber().equals(fromBlock)) // remove all logs of the fromBlock
                .map(this::toIdentifierFramePair)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> result));
        return result;
    }

    /**
     * Returns the number of the most recent block using eth_blockNumber.
     * https://wiki.parity.io/JSONRPC-eth-module#eth_blocknumber
     *
     * @return BigInteger of the current block number the client is on.
     */
    public BigInteger blockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber();
        } catch (IOException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Starts a {@link Flowable} subscription on the connected blockchain using the eth_pubsub module.
     * Notifies the given {@code frameListener} about new frames received from the blockchain matching the {@link ParityAdapter#EVENT_TOPIC}.
     *
     * @param webSocketURI uri of the parity websocket endpooint
     * @param frameListener object that implements {@link FrameListener} to notify about new frames received from the connected blockchain
     * @throws ConnectException if there was a problem connecting to the {@code webSocketURI}.
     */
    public void startFrameListener(URI webSocketURI, FrameListener frameListener) throws ConnectException {
        if(this.eventSubscription != null)
            return;

        WebSocketService service = new WebSocketService(new WebSocketClient(webSocketURI), false);
        service.connect();

        // topics to filter for
        List<String> topics = Collections.singletonList(EVENT_TOPIC);

        Flowable<LogNotification> notifications = Web3j.build(service).logsNotifications(CONTRACT_ADDRESS, topics);
        this.eventSubscription = notifications.subscribe(logNotification -> {
            Log log = logNotification.getParams().getResult();

            // retrieve the indexed data identifier, which is the second topic [event topic, identifier]
            String identifier = log.getTopics().get(1).replaceFirst(HEX_PREFIX, "");

            // retreive the non indexed values (input parameters)
            List<Type> nonIndexedValues = FunctionReturnDecoder.decode(log.getData(), EventStorage.DATAEVENT_EVENT.getNonIndexedParameters());

            // the only input parameter that is not indexed is the serialized frame
            byte[] serializedFrame = (byte[]) nonIndexedValues.get(0).getValue();
            frameListener.update(Hex.decodeHex(identifier), serializedFrame);
        });
    }

    /**
     * Disposes any open {@link Flowable} subscription.
     */
    public void stopFrameListener() {
        if(this.eventSubscription != null && !this.eventSubscription.isDisposed()) {
            this.eventSubscription.dispose();
            this.eventSubscription = null;
        }
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

    private List<EthLog.LogResult> getLogs(byte[] identifier, BigInteger fromBlock, BigInteger toBlock, Integer limit) {
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
            throw new MisconfigurationException(e.getMessage());
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
            throw new MisconfigurationException("Unexpected result type: " + logResult.get().getClass() + " required LogObject");
        }
    }

    private EventValues parseLogResult(EthLog.LogResult logResult) {
        EthLog.LogObject log = castToLogObject(logResult);
        return Contract.staticExtractEventParameters(EventStorage.DATAEVENT_EVENT, log);
    }
}
