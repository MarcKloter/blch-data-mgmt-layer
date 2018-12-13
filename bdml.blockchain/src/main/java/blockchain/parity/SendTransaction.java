package blockchain.parity;

import bdml.blockchain.jsonrpc.JsonRpcRequest;

public class SendTransaction extends JsonRpcRequest {
	public Object[] params;

	public class Transaction {
		public String from;
		public String data;

		public Transaction(String from, String data) {
			this.from = from;
			this.data = data;
		}
	}

	/**
	 * https://wiki.parity.io/JSONRPC-personal-module#personal_sendtransaction
	 */
	public SendTransaction(String id, String from, String data, String password) {
		super(id, "personal_sendTransaction");
		this.params = new Object[] { new Transaction(from, data), password };
	}
}
