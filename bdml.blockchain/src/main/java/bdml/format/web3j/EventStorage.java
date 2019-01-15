package bdml.format.web3j;

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
 * <p>Generated with web3j version 4.1.0.
 */
public class EventStorage extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b506101df806100206000396000f3fe608060405234801561001057600080fd5b5060043610610048576000357c010000000000000000000000000000000000000000000000000000000090048063d8c1e6151461004d575b600080fd5b6101106004803603604081101561006357600080fd5b81019080803590602001909291908035906020019064010000000081111561008a57600080fd5b82018360208201111561009c57600080fd5b803590602001918460018302840111640100000000831117156100be57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610112565b005b817f463dda64e4b1935a0560df1fec5454c1114c1ccce3cd202071c12d1aac232e87826040518080602001828103825283818151815260200191508051906020019080838360005b8381101561017557808201518184015260208101905061015a565b50505050905090810190601f1680156101a25780820380516001836020036101000a031916815260200191505b509250505060405180910390a2505056fea165627a7a7230582041fe38899765a52b8c62d09e8955ca9b0805742bcccffd0e736b726b589cc9c00029";

    public static final String FUNC_NEWDATA = "newData";

    public static final Event DATAEVENT_EVENT = new Event("DataEvent", 
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

    public RemoteCall<TransactionReceipt> newData(BigInteger identifier, byte[] frame) {
        final Function function = new Function(
                FUNC_NEWDATA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(identifier), 
                new org.web3j.abi.datatypes.DynamicBytes(frame)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public List<DataEventEventResponse> getDataEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DATAEVENT_EVENT, transactionReceipt);
        ArrayList<DataEventEventResponse> responses = new ArrayList<DataEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DataEventEventResponse typedResponse = new DataEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.frame = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DataEventEventResponse> dataEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, DataEventEventResponse>() {
            @Override
            public DataEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DATAEVENT_EVENT, log);
                DataEventEventResponse typedResponse = new DataEventEventResponse();
                typedResponse.log = log;
                typedResponse.identifier = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.frame = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DataEventEventResponse> dataEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DATAEVENT_EVENT));
        return dataEventEventFlowable(filter);
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

    public static class DataEventEventResponse {
        public Log log;

        public BigInteger identifier;

        public byte[] frame;
    }
}
