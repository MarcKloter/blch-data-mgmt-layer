package blockchain.parity;

import bdml.blockchain.jsonrpc.JsonRpcResponse;

public class GetTransactionByHashResponse extends JsonRpcResponse {
	public Transaction result;
	
	public class Transaction {
		public String hash;
		public String nonce;
		public String blockHash;
		public String blockNumber;
		public String transactionIndex;
		public String from;
		public String to;
		public String value;
		public String gasPrice;
		public String gas;
		public String input;
		public String v;
		public String standardV;
		public String r;
		public String raw;
		public String publicKey;
		public String chainId;
		public String creates;
		public String condition;
	}
}
