package bdml.blockchain;

import bdml.blockchain.parity.ParityAdapter;
import bdml.blockchain.persistence.AccountMap;
import bdml.services.Blockchain;
import bdml.services.api.types.Account;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.exceptions.MissingConfigurationException;
import bdml.services.helper.FrameListener;

import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * The BlockchainFacade implements the Blockchain interface and performs context-specific input validation.
 */
public class BlockchainFacade implements Blockchain {
    // mandatory configuration properties
    private static final String OUTPUT_DIRECTORY_KEY = "bdml.output.directory";
    private static final String CONTRACT_ADDRESS_KEY = "bdml.blockchain.contract.address";
    private static final String URI_KEY = "bdml.blockchain.parity.jsonrpc.uri";
    private static final String WEBSOCKET_URI_KEY = "bdml.blockchain.parity.websocket.uri";

    private final String webSocketURI;

    private AccountMap accounts;
    private ParityAdapter parity;

    public BlockchainFacade(Properties configuration) {
        // load configuration
        String outputDirectory = getProperty(configuration, OUTPUT_DIRECTORY_KEY);
        String contractAddress = getProperty(configuration, CONTRACT_ADDRESS_KEY);
        String jsonrpcURI = getProperty(configuration, URI_KEY);
        this.webSocketURI = getProperty(configuration, WEBSOCKET_URI_KEY);

        this.accounts = new AccountMap(outputDirectory);
        this.parity = new ParityAdapter(jsonrpcURI, contractAddress);
    }

    private String getProperty(Properties configuration, String property) {
        if(!configuration.containsKey(property))
            throw new MissingConfigurationException(property);

        return configuration.getProperty(property);
    }

    @Override
    public String createEntity(Account account) {
        String address = parity.createAccount(account.getPassword());

        // store the address associated to the given id
        accounts.put(account.getIdentifier(), address);

        return address;
    }

    @Override
    public void storeFrame(Account account, byte[] identifier, byte[] frame) {
        Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");
        Objects.requireNonNull(frame, "Parameter 'frame' cannot be null.");

        if(identifier.length != 32)
            throw new IllegalArgumentException(String.format("The parameter identifier is %d bytes, expected 32 bytes.", identifier.length));

        if(frame.length == 0)
            throw new IllegalArgumentException("The parameter payload is empty.");

        String fromAddress = accounts.get(account.getIdentifier());

        // require the caller to initialize an entity before using the account
        // an entity has to be created beforehand to allow the caller to transfer coins to pay the GAS costs
        if(fromAddress == null)
            throw new IllegalStateException("There was no associated entity found for the given account. Please initialize it by calling createEntity.");

        parity.storeData(fromAddress, account.getPassword(), identifier, frame);
    }

    @Override
    public String blockPointer() {
        return parity.blockNumber().toString(16);
    }

    @Override
    public byte[] getFrame(byte[] identifier) {
        Objects.requireNonNull(identifier, "Parameter 'identifier' cannot be null.");

        if(identifier.length != 32)
            throw new IllegalArgumentException(String.format("The parameter identifier is %d bytes, expected 32 bytes.", identifier.length));

        return parity.getFrame(identifier);
    }

    @Override
    public LinkedHashSet<Map.Entry<byte[], byte[]>> getFrames(String fromBlock) {
        Objects.requireNonNull(fromBlock, "Parameter 'fromBlock' cannot be null.");

        BigInteger from = new BigInteger(fromBlock, 16);
        BigInteger current = parity.blockNumber();
        if(from.compareTo(current) > 0)
            throw new IllegalArgumentException("The given 'fromBlock' does not exist.");

        return parity.getAllFrames(from);
    }

    @Override
    public void startFrameListener(FrameListener frameListener) {
        try {
            URI uri = new URI(webSocketURI);
            parity.startFrameListener(uri, frameListener);
        } catch (URISyntaxException e) {
            throw new MisconfigurationException(String.format("WebSocket URI malformed: %s", e.getMessage()));
        } catch (ConnectException e) {
            throw new MisconfigurationException(String.format("Cannot connect to WebSocket: %s", e.getMessage()));
        }
    }

    @Override
    public void stopFrameListener() {
        parity.stopFrameListener();
    }
}
