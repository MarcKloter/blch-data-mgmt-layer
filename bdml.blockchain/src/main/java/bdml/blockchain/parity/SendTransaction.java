package bdml.blockchain.parity;

import bdml.blockchain.jsonrpc.JsonRpcRequest;

public class SendTransaction extends JsonRpcRequest {
	public Object[] params;

	/**
	 * https://wiki.parity.io/JSONRPC-personal-module#personal_sendtransaction
	 */
	public SendTransaction(String id, String from, String data, String password) {
		super(id, "personal_sendTransaction");
		this.params = new Object[] { new Transaction.Builder().withFrom(from).withData(data).build(), password };
	}
}
