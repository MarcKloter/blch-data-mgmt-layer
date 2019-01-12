package bdml.blockchain;

import bdml.blockchain.parity.ParityAdapter;
import bdml.blockchain.persistence.AccountMap;
import bdml.services.Blockchain;
import bdml.services.api.types.Account;
import bdml.services.exceptions.MisconfigurationException;
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
    // TODO: load from config file
    private final String CONTRACT_ADDRESS = "0x964bc870a2d3e8bf73d05fa5708039bc1861a118";
    private final String URL = "http://localhost:8545";
    private final String WEBSOCKET_URI = "ws://localhost:8546";

    private AccountMap accounts;
    private ParityAdapter parity;

    public BlockchainFacade() {
        this.accounts = new AccountMap();
        this.parity = new ParityAdapter(URL, CONTRACT_ADDRESS);
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
        URI webSocketURI;
        try {
            webSocketURI = new URI(WEBSOCKET_URI);
            parity.startFrameListener(webSocketURI, frameListener);
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
