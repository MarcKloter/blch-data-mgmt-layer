package bdml.blockchain.parity;

import bdml.blockchain.jsonrpc.JsonRpcRequest;

public class GetTransactionByHash extends JsonRpcRequest {
	public String[] params;

	/**
	 * https://wiki.parity.io/JSONRPC-eth-module#eth_gettransactionbyhash
	 */
	public GetTransactionByHash(String id, String hash) {
		super(id, "eth_getTransactionByHash");
		this.params = new String[] { hash };
	}
}
