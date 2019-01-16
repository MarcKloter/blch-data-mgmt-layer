package bdml.blockchain.parity;

import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.util.List;

/**
 * Adds the Parity specific limit filter option to the web3j EthFilter.
 * https://wiki.parity.io/JSONRPC-eth-module#eth_newfilter
 */
public class ParityFilter extends EthFilter {
    private int limit;

    public ParityFilter(DefaultBlockParameter fromBlock, DefaultBlockParameter toBlock, List<String> address, int limit) {
        super();
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}
