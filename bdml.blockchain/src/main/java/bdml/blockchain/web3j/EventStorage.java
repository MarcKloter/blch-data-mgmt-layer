package bdml.blockchain.web3j;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint160;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.1.1.
 */
public class EventStorage extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b50610562806100206000396000f3fe608060405234801561001057600080fd5b5060043610610068577c0100000000000000000000000000000000000000000000000000000000600035046354b70595811461006d5780637032d7b41461011c57806381d8d6e1146101df578063c1b11b84146102a2575b600080fd5b61011a6004803603604081101561008357600080fd5b813591908101906040810160208201356401000000008111156100a557600080fd5b8201836020820111156100b757600080fd5b803590602001918460018302840111640100000000831117156100d957600080fd5b91908080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525092955061034f945050505050565b005b61011a6004803603604081101561013257600080fd5b73ffffffffffffffffffffffffffffffffffffffff823516919081019060408101602082013564010000000081111561016a57600080fd5b82018360208201111561017c57600080fd5b8035906020019184600183028401116401000000008311171561019e57600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295506103ed945050505050565b61011a600480360360408110156101f557600080fd5b73ffffffffffffffffffffffffffffffffffffffff823516919081019060408101602082013564010000000081111561022d57600080fd5b82018360208201111561023f57600080fd5b8035906020019184600183028401116401000000008311171561026157600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250929550610462945050505050565b61011a600480360360408110156102b857600080fd5b813591908101906040810160208201356401000000008111156102da57600080fd5b8201836020820111156102ec57600080fd5b8035906020019184600183028401116401000000008311171561030e57600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295506104d7945050505050565b817f29963ea0817fc4a47fe805a578bb99ecf970fb126fc54fa8567b850b8a62f210826040518080602001828103825283818151815260200191508051906020019080838360005b838110156103af578181015183820152602001610397565b50505050905090810190601f1680156103dc5780820380516001836020036101000a031916815260200191505b509250505060405180910390a25050565b8173ffffffffffffffffffffffffffffffffffffffff167f135fd42d6f38557b07f09553227db5e33e3804ab21a45e7a186fecbdcbdacbff82604051808060200182810382528381815181526020019150805190602001908083836000838110156103af578181015183820152602001610397565b8173ffffffffffffffffffffffffffffffffffffffff167fd04e38b583a1a2b090cb4dd2eaab51402c242c7da53604493eec36530fb3770b82604051808060200182810382528381815181526020019150805190602001908083836000838110156103af578181015183820152602001610397565b817f0df7f0e88502e9461f84c9488435754a805319f4af479ca0181fd4d81b5d18b282604051808060200182810382528381815181526020019150805190602001908083836000838110156103af57818101518382015260200161039756fea165627a7a72305820e74d1355200fae626321cbcc3b9547e283cad47ef7d10b34db09ed5896fb68150029";

    public static final String FUNC_NEWSECRETDATA = "newSecretData";

    public static final String FUNC_NEWACCESS = "newAccess";

    public static final String FUNC_NEWAMEND = "newAmend";

    public static final String FUNC_NEWPUBLICDATA = "newPublicData";

    public static final Event SECRETDATAEVENT_EVENT = new Event("SecretDataEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event PUBLICDATAEVENT_EVENT = new Event("PublicDataEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event ACCESSEVENT_EVENT = new Event("AccessEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint160>(true) {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event AMENDEVENT_EVENT = new Event("AmendEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint160>(true) {}, new TypeReference<DynamicBytes>() {}));
    ;

    @Deprecated
    protected EventStorage(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected EventStorage(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected EventStorage(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected EventStorage(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> newSecretData(BigInteger identifier, byte[] document) {
        final Function function = new Function(
                FUNC_NEWSECRETDATA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(identifier), 
                new org.web3j.abi.datatypes.DynamicBytes(document)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> newAccess(BigInteger identifier, byte[] token) {
        final Function function = new Function(
                FUNC_NEWACCESS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint160(identifier), 
                new org.web3j.abi.datatypes.DynamicBytes(token)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> newAmend(BigInteger identifier, byte[] token) {
        final Function function = new Function(
                FUNC_NEWAMEND, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint160(identifier), 
                new org.web3j.abi.datatypes.DynamicBytes(token)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> newPublicData(BigInteger identifier, byte[] document) {
        final Function function = new Function(
                FUNC_NEWPUBLICDATA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(identifier), 
                new org.web3j.abi.datatypes.DynamicBytes(document)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public List<SecretDataEventEventResponse> getSecretDataEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(SECRETDATAEVENT_EVENT, transactionReceipt);
        ArrayList<SecretDataEventEventResponse> responses = new ArrayList<SecretDataEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SecretDataEventEventResponse typedResponse = new SecretDataEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.document = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<SecretDataEventEventResponse> secretDataEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, SecretDataEventEventResponse>() {
            @Override
            public SecretDataEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(SECRETDATAEVENT_EVENT, log);
                SecretDataEventEventResponse typedResponse = new SecretDataEventEventResponse();
                typedResponse.log = log;
                typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.document = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<SecretDataEventEventResponse> secretDataEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SECRETDATAEVENT_EVENT));
        return secretDataEventEventFlowable(filter);
    }

    public List<PublicDataEventEventResponse> getPublicDataEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(PUBLICDATAEVENT_EVENT, transactionReceipt);
        ArrayList<PublicDataEventEventResponse> responses = new ArrayList<PublicDataEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PublicDataEventEventResponse typedResponse = new PublicDataEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.document = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PublicDataEventEventResponse> publicDataEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, PublicDataEventEventResponse>() {
            @Override
            public PublicDataEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(PUBLICDATAEVENT_EVENT, log);
                PublicDataEventEventResponse typedResponse = new PublicDataEventEventResponse();
                typedResponse.log = log;
                typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.document = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PublicDataEventEventResponse> publicDataEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PUBLICDATAEVENT_EVENT));
        return publicDataEventEventFlowable(filter);
    }

    public List<AccessEventEventResponse> getAccessEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ACCESSEVENT_EVENT, transactionReceipt);
        ArrayList<AccessEventEventResponse> responses = new ArrayList<AccessEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AccessEventEventResponse typedResponse = new AccessEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.token = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<AccessEventEventResponse> accessEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, AccessEventEventResponse>() {
            @Override
            public AccessEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ACCESSEVENT_EVENT, log);
                AccessEventEventResponse typedResponse = new AccessEventEventResponse();
                typedResponse.log = log;
                typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.token = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<AccessEventEventResponse> accessEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ACCESSEVENT_EVENT));
        return accessEventEventFlowable(filter);
    }

    public List<AmendEventEventResponse> getAmendEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(AMENDEVENT_EVENT, transactionReceipt);
        ArrayList<AmendEventEventResponse> responses = new ArrayList<AmendEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AmendEventEventResponse typedResponse = new AmendEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.token = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<AmendEventEventResponse> amendEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, AmendEventEventResponse>() {
            @Override
            public AmendEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(AMENDEVENT_EVENT, log);
                AmendEventEventResponse typedResponse = new AmendEventEventResponse();
                typedResponse.log = log;
                typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.token = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<AmendEventEventResponse> amendEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(AMENDEVENT_EVENT));
        return amendEventEventFlowable(filter);
    }

    @Deprecated
    public static EventStorage load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new EventStorage(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static EventStorage load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new EventStorage(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static EventStorage load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new EventStorage(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static EventStorage load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new EventStorage(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<EventStorage> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(EventStorage.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<EventStorage> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(EventStorage.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<EventStorage> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(EventStorage.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<EventStorage> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(EventStorage.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class SecretDataEventEventResponse {
        public Log log;

        public BigInteger identifier;

        public byte[] document;
    }

    public static class PublicDataEventEventResponse {
        public Log log;

        public BigInteger identifier;

        public byte[] document;
    }

    public static class AccessEventEventResponse {
        public Log log;

        public BigInteger identifier;

        public byte[] token;
    }

    public static class AmendEventEventResponse {
        public Log log;

        public BigInteger identifier;

        public byte[] token;
    }
}
