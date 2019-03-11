package bdml.blockchain.eth;

import bdml.blockchain.cache.Cache;
import bdml.blockchain.cache.CacheImpl;
import bdml.blockchain.cache.TxtIndex;
import bdml.blockchain.web3j.EventStorage;
import bdml.services.BlockTime;
import bdml.services.Blockchain;
import bdml.services.QueryResult;
import bdml.services.Pair;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.exceptions.MissingConfigurationException;
import io.reactivex.disposables.Disposable;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class EthWeb3Adapter {
    private static final String HEX_PREFIX = "0x";
    private final String PUBLIC_DATA_EVENT_TOPIC = EventEncoder.encode(EventStorage.PUBLICDATAEVENT_EVENT);
    private final String SECRET_DATA_EVENT_TOPIC = EventEncoder.encode(EventStorage.SECRETDATAEVENT_EVENT);
    private final String ACCESS_EVENT_TOPIC = EventEncoder.encode(EventStorage.ACCESSEVENT_EVENT);
    private final String AMEND_EVENT_TOPIC = EventEncoder.encode(EventStorage.AMENDEVENT_EVENT);

    private final List<String> CONTRACT_ADDRESS;
    private static final String CONTRACT_ADDRESS_KEY = "bdml.blockchain.contract.address";
    private static final String URI_KEY = "bdml.blockchain.parity.jsonrpc.uri";
    private static final String WEBSOCKET_URI_KEY = "bdml.blockchain.parity.websocket.uri";
    private static final String FINALIZATION_DEPTH_KEY = "bdml.blockchain.parity.finalization.depth";

    private Admin web3j;
    private Cache cache;
    private Blockchain.BlockFinalizedListener listener;


    public EthWeb3Adapter(Properties configuration) {
        this(configuration,null);
    }

    public EthWeb3Adapter(Properties configuration, Blockchain.BlockFinalizedListener listener) {
        String contractAddress = getProperty(configuration, CONTRACT_ADDRESS_KEY);
        String url = getProperty(configuration, URI_KEY);
        String webSocketURI = getProperty(configuration, WEBSOCKET_URI_KEY);
        String finalizationUnparsed = getProperty(configuration, FINALIZATION_DEPTH_KEY);
        int finalization;
        try {
            finalization = Integer.parseInt(finalizationUnparsed);
        } catch (NumberFormatException e) {
            throw new MissingConfigurationException(FINALIZATION_DEPTH_KEY);
        }
        this.web3j = Admin.build(new HttpService(url));
        this.CONTRACT_ADDRESS = Collections.singletonList(contractAddress);
        this.cache = new CacheImpl(configuration);
        checkContract();
        startDataListener(webSocketURI, finalization, listener);
    }

    private String getProperty(Properties configuration, String property) {
        if (!configuration.containsKey(property))
            throw new MissingConfigurationException(property);

        return configuration.getProperty(property);
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


    private EventStorage getContract(){
        //we can go ahead
        try {
            //fresh key
            TransactionManager transactionManager = new AnonymousTransactionManager(web3j);
            // TODO: GAS LIMIT has to be determined dynamically
            // TODO: take a look at https://github.com/web3j/web3j/blob/master/integration-tests/src/test/java/org/web3j/protocol/scenarios/EventFilterIT.java
            BigInteger GAS_PRICE = BigInteger.valueOf(0x0);
            BigInteger GAS_LIMIT = BigInteger.valueOf(0xfffff);

            ContractGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
            return EventStorage.load(CONTRACT_ADDRESS.get(0), web3j, transactionManager, gasProvider);
        } catch (Exception e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Calls the newData method of the deployed smart contract using personal_sendTransaction.
     * https://wiki.parity.io/JSONRPC-personal-module#personal_sendtransaction
     *
     * @param identifier 20 bytes identifier as input for the newData method
     * @param document bytes array containing the frame as input for the newData method
     * @param encrypted indicates if bytes are encrypted or not
     * @return false if the id was already in use and true otherwise
     */
    public boolean storeData(byte[] identifier, byte[] document, boolean encrypted) {
        //Check that not already their
        List<QueryResult<byte[]>> cur_doc = getDocument(identifier, true); //todo: we could shortcut to checkFrameExists (would not waste lookup)
        if(cur_doc.isEmpty()) {
            //we can go ahead
            try {
                String encIdent = Hex.encodeHexString(identifier);
                BigInteger numIdent = new BigInteger(encIdent, 16);
                EventStorage storage = getContract();
                if(encrypted) {
                    System.out.println("Store(Data -  Enc): "+encIdent);
                    storage.newSecretData(numIdent, document).send();
                } else {
                    System.out.println("Store(Data - Plain): " + encIdent);
                    storage.newPublicData(numIdent, document).send();
                }
                cache.addPendingFrame(identifier,document, encrypted);
                return true;
            } catch (Exception e) {
                throw new MisconfigurationException(e.getMessage());
            }
        } else {
            return false;
        }
    }

    public void storeToken(byte[] identifier, byte[] token) {
        try {
            System.out.println("Store(Access): "+new String(Hex.encodeHex(identifier)));
            getContract().newAccess(new BigInteger(Hex.encodeHexString(identifier), 16), token).send();
            cache.addPendingToken(identifier,token);
        } catch (Exception e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }


    public void storeAmend(byte[] identifier, byte[] token) {
        try {
            System.out.println("Store(Amend): "+new String(Hex.encodeHex(identifier)));
            getContract().newAmend(new BigInteger(Hex.encodeHexString(identifier), 16), token).send();
            cache.addPendingAmend(identifier,token);
        } catch (Exception e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    /**
     * Queries event logs for an indexed identifier using eth_getLogs.
     * https://wiki.parity.io/JSONRPC-eth-module#eth_getlogs
     *
     * @param identifier 32 bytes indexed event identifier
     * @return Byte array containing the frame within the event log matching the given {@code identifier}.
     */
    public List<QueryResult<byte[]>> getDocument(byte[] identifier, boolean includePending) {

        QueryResult<byte[]> pending = null;
        if(includePending) {
            pending = cache.getPendingDocument(identifier);
        }

        if(pending != null) {
            return List.of(pending);
        } else {
            Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");

            return cache.getIndex(identifier).stream()
                    .flatMap( index -> {
                        try {
                            return web3j.ethGetTransactionReceipt(HEX_PREFIX + Hex.encodeHexString(index.hash)).send().getTransactionReceipt()
                                    .flatMap(r -> extractDocuments(r.getLogs()))
                                    .map(l -> new QueryResult<>(retrieveDocument(l,index.isPlain), new BlockTime(index.blockNo, index.txtIndex), index.isPlain))
                                    .stream();
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * Queries all token event logs newer than {@code fromBlock} using eth_getLogs.
     * https://wiki.parity.io/JSONRPC-eth-module#eth_getlogs
     *
     * @param fromBlockInclusive block number to start receiving frames from
     * @param identifier filters for data addressed to identifier
     * @return Set containing all txtHashes of accessTokens stored after the {@code fromBlock}
     */
    public List<QueryResult<byte[]>> getAllTokens(long fromBlockInclusive, long toBlockExclusive, byte[] identifier) {
        return cache.getAllFinalizedAccessTokens(fromBlockInclusive,toBlockExclusive,identifier).stream().
                flatMap( hash -> {
                    try {
                        return web3j.ethGetTransactionReceipt(HEX_PREFIX + Hex.encodeHexString(hash)).send().getTransactionReceipt().stream();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .flatMap( receipt -> extract(receipt.getLogs(), ACCESS_EVENT_TOPIC).stream())
                .map(l -> new QueryResult<>(
                        retrieveLogData(EventStorage.ACCESSEVENT_EVENT, l).second,
                        new BlockTime(l.getBlockNumber().longValue(),l.getTransactionIndex().longValue()),
                        false
                ))
                .collect(Collectors.toList());
    }


    public List<QueryResult<byte[]>> getAllPlainIds(long fromBlockInclusive, long toBlockExclusive) {
        return cache.getAllPlain(fromBlockInclusive,toBlockExclusive);
    }

    //todo: return identifier + token
    public List<QueryResult<Pair<byte[],byte[]>>> getAllAmendmentTokens(long fromBlockInclusive, long toBlockExclusive) {
        return cache.getAllAmendments(fromBlockInclusive,toBlockExclusive).stream().
                flatMap( hash -> {
                    try {
                        return web3j.ethGetTransactionReceipt(HEX_PREFIX + Hex.encodeHexString(hash)).send().getTransactionReceipt().stream();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .flatMap( receipt -> extract(receipt.getLogs(), AMEND_EVENT_TOPIC).stream())
                .map(l -> new QueryResult<>(
                        retrieveLogData(EventStorage.AMENDEVENT_EVENT, l),
                        new BlockTime(l.getBlockNumber().longValue(),l.getTransactionIndex().longValue()),
                        false
                ))
                .collect(Collectors.toList());
    }

    public List<QueryResult<byte[]>> getAllAmendmentTokensFor(byte[] identifier) {
        return cache.getAllAmendmentFor(identifier).stream().
                flatMap(hash -> {
                    try {
                        return web3j.ethGetTransactionReceipt(HEX_PREFIX + Hex.encodeHexString(hash)).send().getTransactionReceipt().stream();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .flatMap(receipt -> extract(receipt.getLogs(), AMEND_EVENT_TOPIC).stream())
                .map(l -> new QueryResult<>(
                        retrieveLogData(EventStorage.AMENDEVENT_EVENT, l).second,
                        new BlockTime(l.getBlockNumber().longValue(),l.getTransactionIndex().longValue()),
                        false
                ))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of the most next block
     * @return BigInteger of the next block number the client will process.
     */
    public long nextBlock() {
        return cache.nextBlock();
    }

    private void processDataEvent(BigInteger finalBlock, EthLog.LogResult log, boolean isPlain) {
        try {
            if(log instanceof EthLog.LogObject) {
                EthLog.LogObject logObj = (EthLog.LogObject)log;
                String identifier = logObj.getTopics().get(1);
                identifier = identifier.replaceFirst(HEX_PREFIX, "");
                String hash = logObj.getTransactionHash();
                hash = hash.replaceFirst(HEX_PREFIX, "");
                byte[] byteId = Hex.decodeHex(identifier);
                byte[] bytehash = Hex.decodeHex(hash);
                cache.addDocumentIndex(byteId, new TxtIndex(finalBlock.longValue(), logObj.getTransactionIndex().longValue(), bytehash, isPlain));
                QueryResult<byte[]> pending = cache.getPendingDocument(byteId);
                if(pending != null && pending.plain == isPlain) {
                    byte[] newFrame = retrieveDocument(logObj, isPlain);
                    if(Arrays.equals(newFrame, pending.data)) {
                        System.out.println("Committed(Data): "+identifier);
                        cache.removePendingFrame(byteId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processBlockPrivateDataEvents(BigInteger finalBlock) throws IOException {
        EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(finalBlock), DefaultBlockParameter.valueOf(finalBlock), CONTRACT_ADDRESS);
        filter.addSingleTopic(SECRET_DATA_EVENT_TOPIC);
        web3j.ethGetLogs(filter).send().getLogs().forEach(log -> processDataEvent(finalBlock,log,false));
    }

    private void processBlockPublicDataEvents(BigInteger finalBlock) throws IOException {
        EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(finalBlock), DefaultBlockParameter.valueOf(finalBlock), CONTRACT_ADDRESS);
        filter.addSingleTopic(PUBLIC_DATA_EVENT_TOPIC);
        web3j.ethGetLogs(filter).send().getLogs().forEach(log -> processDataEvent(finalBlock,log,true));
    }

    private void processBlockAccessEvents(BigInteger finalBlock) throws IOException {
        EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(finalBlock), DefaultBlockParameter.valueOf(finalBlock), CONTRACT_ADDRESS);
        filter.addSingleTopic(ACCESS_EVENT_TOPIC);
        web3j.ethGetLogs(filter).send().getLogs().forEach(log -> {
            try {
                if(log instanceof EthLog.LogObject) {
                    EthLog.LogObject logObj = (EthLog.LogObject)log;
                    String identifier = logObj.getTopics().get(1);
                    identifier = identifier.replaceFirst(HEX_PREFIX, "").substring(24);
                    String hash = logObj.getTransactionHash();
                    hash = hash.replaceFirst(HEX_PREFIX, "");
                    byte[] byteId = Hex.decodeHex(identifier);
                    byte[] bytehash = Hex.decodeHex(hash);
                    cache.addAccessIndex(byteId, finalBlock.longValue(), bytehash);
                    cache.removePendingToken(byteId, bytehash);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void processBlockAmendEvents(BigInteger finalBlock) throws IOException {
        EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(finalBlock), DefaultBlockParameter.valueOf(finalBlock), CONTRACT_ADDRESS);
        filter.addSingleTopic(AMEND_EVENT_TOPIC);
        web3j.ethGetLogs(filter).send().getLogs().forEach(log -> {
            try {
                if(log instanceof EthLog.LogObject) {
                    EthLog.LogObject logObj = (EthLog.LogObject)log;
                    String identifier = logObj.getTopics().get(1);
                    identifier = identifier.replaceFirst(HEX_PREFIX, "").substring(24);
                    String hash = logObj.getTransactionHash();
                    hash = hash.replaceFirst(HEX_PREFIX, "");
                    byte[] byteId = Hex.decodeHex(identifier);
                    byte[] bytehash = Hex.decodeHex(hash);
                    cache.addAmendIndex(byteId, finalBlock.longValue(), bytehash);
                    cache.removePendingAmend(byteId, bytehash);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    //The new workhorse needs commenting
    private void startDataListener(String webSocketURI, int finalizationDelay, Blockchain.BlockFinalizedListener listener) {
        try {
            WebSocketService service = new WebSocketService(new WebSocketClient(new URI(webSocketURI)), false);
            service.connect();
            long next = cache.nextBlock();
            Disposable eventSubscription = web3j.replayPastAndFutureBlocksFlowable(DefaultBlockParameter.valueOf(BigInteger.valueOf(next)), false).subscribe(block -> {
                BigInteger curBlock = block.getBlock().getNumber();
                BigInteger finalBlock = curBlock.subtract(BigInteger.valueOf(finalizationDelay));
                long finalBlockNo = finalBlock.longValue();
                if(finalBlockNo >= cache.nextBlock()) {
                    // topics to filter for
                    processBlockPrivateDataEvents(finalBlock);
                    processBlockPublicDataEvents(finalBlock);
                    processBlockAccessEvents(finalBlock);
                    processBlockAmendEvents(finalBlock);
                    System.out.println("Block-Processed:"+finalBlock);
                    cache.finalizeBlock(finalBlockNo);
                    if(listener != null) {
                        try {
                            listener.newBlockFinalized(finalBlockNo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(new Thread(eventSubscription::dispose));

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }



    private Optional<Log> extractDocuments(List<Log> logs) {
        return logs.stream().filter(l -> {
            List<String> topics =  l.getTopics();
            return topics.contains(PUBLIC_DATA_EVENT_TOPIC) || topics.contains(SECRET_DATA_EVENT_TOPIC);
        }).findFirst();
    }

    private Optional<Log> extract(List<Log> logs, String event) {
        return logs.stream().filter(l -> {
            List<String> topics =  l.getTopics();
            return topics.contains(event);
        }).findFirst();
    }



    private Pair<byte[],byte[]> retrieveLogData(Event event, Log logResult) {
        EventValues eventValues = Contract.staticExtractEventParameters(event, logResult);
        BigInteger idBig = (BigInteger)eventValues.getIndexedValues().get(0).getValue();
        try {
            byte[] id = Hex.decodeHex(String.format("%064x", idBig));
            byte[] body = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            return new Pair<>(id,body);
        } catch (DecoderException e) {
            throw new MisconfigurationException("EventStorage Log had unexpected format: " + e.getMessage());
        }
    }

    private byte[] retrieveDocument(Log logResult, boolean isPlain) {
        if(isPlain){
            return retrieveLogData(EventStorage.PUBLICDATAEVENT_EVENT, logResult).second;
        } else {
            return retrieveLogData(EventStorage.SECRETDATAEVENT_EVENT, logResult).second;
        }
    }


}
