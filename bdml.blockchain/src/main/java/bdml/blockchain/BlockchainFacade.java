package bdml.blockchain;

import bdml.blockchain.callback.BlockFinalizationCallbackManager;
import bdml.services.QueryResult;
import bdml.blockchain.eth.EthWeb3Adapter;
import bdml.blockchain.persistence.AccountMap;
import bdml.services.Blockchain;
import bdml.services.Pair;
import bdml.services.exceptions.MissingConfigurationException;

import java.math.BigInteger;
import java.util.*;

/**
 * The BlockchainFacade implements the Blockchain interface and performs context-specific input validation.
 */
public class BlockchainFacade implements Blockchain {
    private EthWeb3Adapter parity;
    private BlockFinalizationCallbackManager listenerManager;

    public BlockchainFacade(Properties configuration) {
        // load configuration
        this.listenerManager = new BlockFinalizationCallbackManager();
        this.parity = new EthWeb3Adapter(configuration,listenerManager);

    }


    @Override
    public boolean storeDocument(byte[] identifier, byte[] doc, boolean encrypted) {
        Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");
        Objects.requireNonNull(doc, "Parameter 'doc' cannot be null.");

        if(identifier.length != 32)
            throw new IllegalArgumentException(String.format("The parameter identifier is %d bytes, expected 32 bytes.", identifier.length));

        if(doc.length == 0)
            throw new IllegalArgumentException("The parameter doc is empty.");

        return parity.storeData(identifier, doc, encrypted);
    }


    private void checkTokenInputValidity(byte[] identifier, byte[] token) {
        Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");
        Objects.requireNonNull(token, "Parameter 'token' cannot be null.");

        if(identifier.length != 20)
            throw new IllegalArgumentException(String.format("The parameter identifier is %d bytes, expected 20 bytes.", identifier.length));

        if(token.length == 0)
            throw new IllegalArgumentException("The parameter token is empty.");

    }

    @Override
    public void storeAccessToken(byte[] identifier, byte[] token) {
        checkTokenInputValidity(identifier, token);
        parity.storeToken(identifier, token);
    }

    @Override
    public void storeAmendmentToken(byte[] identifier, byte[] token) {
        checkTokenInputValidity(identifier, token);
        parity.storeAmend(identifier, token);
    }

    @Override
    public long blockPointer() {
        return BigInteger.valueOf(parity.nextBlock()-1).longValueExact();
    }

    @Override
    public List<QueryResult<byte[]>> getDocument(byte[] identifier, boolean includePending) {
        Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");

        if(identifier.length != 32)
            throw new IllegalArgumentException(String.format("The parameter identifier is %d bytes, expected 32 bytes.", identifier.length));

        return parity.getDocument(identifier, includePending);
    }

    private boolean checkBlockRange(long fromBlock, long toBlock){
        Objects.requireNonNull(fromBlock, "Parameter 'fromBlock' cannot be null.");
        Objects.requireNonNull(toBlock, "Parameter 'toBlock' cannot be null.");

        long next = parity.nextBlock();

        if(fromBlock >= toBlock) {
            return false;
        }

        if(toBlock > next) {
            throw new IllegalArgumentException("Invalid BlockRange.");
        }

        return true;
    }

    @Override
    public List<QueryResult<byte[]>> getAllTokens(long fromBlock, long toBlock, byte[] identifier) {
        if(!checkBlockRange(fromBlock,toBlock)){
            return List.of();
        }
        return parity.getAllTokens(fromBlock, toBlock, identifier);
    }

    @Override
    public List<QueryResult<byte[]>> getAllPlainIds(long fromBlock, long toBlock) {
        if(!checkBlockRange(fromBlock,toBlock)){
            return List.of();
        }
        return parity.getAllPlainIds(fromBlock, toBlock);
    }

    @Override
    public List<QueryResult<Pair<byte[],byte[]>>> getAllAmendmentTokens(long fromBlock, long toBlock) {
        if(!checkBlockRange(fromBlock,toBlock)){
            return List.of();
        }

        return parity.getAllAmendmentTokens(fromBlock, toBlock);
    }

    @Override
    public List<QueryResult<byte[]>> getAllAmendmentTokensFor(byte[] identifier) {
        return parity.getAllAmendmentTokensFor(identifier);
    }

    @Override
    public boolean addBlockListener(BlockFinalizedListener listener) {
        Objects.requireNonNull(listener, "Parameter 'listener' cannot be null.");
        return listenerManager.addListener(listener);
    }

    @Override
    public boolean removeBlockListener(BlockFinalizedListener listener) {
        Objects.requireNonNull(listener, "Parameter 'listener' cannot be null.");
        return listenerManager.removeListener(listener);

    }
}
