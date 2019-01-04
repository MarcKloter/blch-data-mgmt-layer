package bdml.blockchain;

import bdml.blockchain.web3j.EventStorage;
import bdml.blockchain.web3j.PersonalTransactionManager;
import org.web3j.codegen.SolidityFunctionWrapperGenerator;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import java.math.BigInteger;

public class Setup {
    /**
     * Generates the java wrapper source code for a smart contract.
     */
    public static void generateJavaSmartContractWrapper() {
        String[] args = {
                "-a", "bdml.blockchain/src/main/resources/EventStorage.abi",
                "-b", "bdml.blockchain/src/main/resources/EventStorage.bin",
                "-o", "bdml.blockchain/src/main/java",
                "-p", "bdml.blockchain.web3j"
        };
        SolidityFunctionWrapperGenerator.main(args);
    }

    /**
     * Deploys the smart contract used in this proof of concept.
     *
     * @param url url of the Parity JSON-RPC endpoint
     * @param fromAddress the address to deploy the contract as
     * @param password passphrase to unlock the fromAddress account
     * @return Address of the deployed smart contract.
     */
    public static String deploySmartContract(String url, String fromAddress, String password) {
        BigInteger GAS_PRICE = BigInteger.valueOf(0x0);
        BigInteger GAS_LIMIT = BigInteger.valueOf(0xfffff);

        Admin web3j = Admin.build(new HttpService(url));
        TransactionManager transactionManager = new PersonalTransactionManager(web3j, fromAddress, password);
        ContractGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

        try {
            Contract contract = EventStorage.deploy(web3j, transactionManager, gasProvider).send();
            return contract.getContractAddress();
        } catch(Exception e) {
            // TODO: handle exception
            throw new RuntimeException();
        }
    }
}
