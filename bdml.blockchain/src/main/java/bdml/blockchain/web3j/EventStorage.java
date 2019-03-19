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
    private static final String BINARY = "608060405234801561001057600080fd5b50610685806100206000396000f3fe60806040526004361061005c576000357c010000000000000000000000000000000000000000000000000000000090048063229f8a8b1461006157806354b70595146101335780637032d7b414610205578063c1b11b84146102ed575b600080fd5b34801561006d57600080fd5b506101316004803603604081101561008457600080fd5b8101908080359060200190929190803590602001906401000000008111156100ab57600080fd5b8201836020820111156100bd57600080fd5b803590602001918460018302840111640100000000831117156100df57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192905050506103bf565b005b34801561013f57600080fd5b506102036004803603604081101561015657600080fd5b81019080803590602001909291908035906020019064010000000081111561017d57600080fd5b82018360208201111561018f57600080fd5b803590602001918460018302840111640100000000831117156101b157600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610460565b005b34801561021157600080fd5b506102eb6004803603604081101561022857600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019064010000000081111561026557600080fd5b82018360208201111561027757600080fd5b8035906020019184600183028401116401000000008311171561029957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610501565b005b3480156102f957600080fd5b506103bd6004803603604081101561031057600080fd5b81019080803590602001909291908035906020019064010000000081111561033757600080fd5b82018360208201111561034957600080fd5b8035906020019184600183028401116401000000008311171561036b57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192905050506105b8565b005b817f15e5772be2fc17c7a61c184883997e2d556d0bd800113fd2c9321d5198441513826040518080602001828103825283818151815260200191508051906020019080838360005b83811015610422578082015181840152602081019050610407565b50505050905090810190601f16801561044f5780820380516001836020036101000a031916815260200191505b509250505060405180910390a25050565b817f29963ea0817fc4a47fe805a578bb99ecf970fb126fc54fa8567b850b8a62f210826040518080602001828103825283818151815260200191508051906020019080838360005b838110156104c35780820151818401526020810190506104a8565b50505050905090810190601f1680156104f05780820380516001836020036101000a031916815260200191505b509250505060405180910390a25050565b8173ffffffffffffffffffffffffffffffffffffffff167f135fd42d6f38557b07f09553227db5e33e3804ab21a45e7a186fecbdcbdacbff826040518080602001828103825283818151815260200191508051906020019080838360005b8381101561057a57808201518184015260208101905061055f565b50505050905090810190601f1680156105a75780820380516001836020036101000a031916815260200191505b509250505060405180910390a25050565b817f0df7f0e88502e9461f84c9488435754a805319f4af479ca0181fd4d81b5d18b2826040518080602001828103825283818151815260200191508051906020019080838360005b8381101561061b578082015181840152602081019050610600565b50505050905090810190601f1680156106485780820380516001836020036101000a031916815260200191505b509250505060405180910390a2505056fea165627a7a7230582057956c96a5014d7187193d85607dc040b4125d37ce45fa6f24600d7fe184416f0029";

    public static final String FUNC_NEWAMEND = "newAmend";

    public static final String FUNC_NEWSECRETDATA = "newSecretData";

    public static final String FUNC_NEWACCESS = "newAccess";

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
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<DynamicBytes>() {}));
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

    public RemoteCall<TransactionReceipt> newAmend(BigInteger identifier, byte[] token) {
        final Function function = new Function(
                FUNC_NEWAMEND, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(identifier), 
                new org.web3j.abi.datatypes.DynamicBytes(token)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
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
