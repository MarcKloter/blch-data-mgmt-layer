package bdml.blockchain;

import bdml.blockchain.persistence.AccountMap;
import bdml.services.Blockchain;
import bdml.services.api.types.Account;

import java.math.BigInteger;
import java.util.Base64;

/**
 * The BlockchainFacade implements the Blockchain interface and performs context-specific input validation.
 */
public class BlockchainFacade implements Blockchain {
    // TODO: load from config file
    private final String CONTRACT_ADDRESS;

    private AccountMap accounts;
    private ParityAdapter blockchain;

    public BlockchainFacade() {
        // TODO: load from config file
        String URL = "http://localhost:8545";
        this.CONTRACT_ADDRESS = "0x964bc870a2d3e8bf73d05fa5708039bc1861a118";
        this.accounts = new AccountMap();
        this.blockchain = new ParityAdapter(URL);
    }

    @Override
    public String createEntity(Account account) {
        String address = blockchain.createAccount(account.getPassword());

        // store the address associated to the given id
        accounts.put(account.getIdentifier(), address);

        return address;
    }

    @Override
    public void createTransaction(Account account, byte[] identifier, byte[] payload) {
        // validate input
        if(identifier.length != 32)
            throw new IllegalArgumentException(String.format("The parameter identifier is %d bytes, expected 32 bytes.", identifier.length));

        if(payload.length == 0)
            throw new IllegalArgumentException("The parameter payload is empty.");

        String fromAddress = accounts.get(account.getIdentifier());

        // require the caller to initialize an entity before using the account
        // an entity has to be created beforehand to allow the caller to transfer coins to pay the GAS costs
        if(fromAddress == null)
            throw new IllegalStateException("There was no associated entity found for the given account. Please initialize it by calling createEntity.");

        blockchain.storeData(fromAddress, account.getPassword(), CONTRACT_ADDRESS, new BigInteger(identifier), payload);
    }

    @Override
    public byte[] getTransaction(byte[] identifier) {
        String payload = "CAESlwEDAOrvsT09bsN2CzrtDFpLMFBD8K4OC15hceSHOMZZB1yq2EJCBgLPlEaXEO9jqTOOzy8YVvzdFaG5MKGunfdvrpZ3+7cShWGqSm12Tn3UgUgwM1pfIa+8LGgV9d6t2FAPEdGnE2S7THYUtpaxHAe0S+i0KdMLMmy43t6JdAW/Xq2uayk2PO3nDoY/9hH6nAN/5WeNUNFFGiAFl/PWCe3yahADssxJFMhcHkjGbN9TjF5QS/qiJxijag==";
        return Base64.getDecoder().decode(payload);
    }
}
