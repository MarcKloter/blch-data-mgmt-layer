package bdml.blockchain;

import blockchain.parity.GetTransactionByHashResponse;

public class Transaction {
	private final String hash;
	private final String nonce;
	private final String blockHash;
	private final String blockNumber;
	private final String transactionIndex;
	private final String from;
	private final String to;
	private final String value;
	private final String gasPrice;
	private final String gas;
	private final String input;
	private final String v;
	private final String standardV;
	private final String r;
	private final String raw;
	private final String publicKey;
	private final String chainId;
	private final String creates;
	private final String condition;

	public Transaction(GetTransactionByHashResponse.Transaction tx) {
		this.hash = tx.hash;
		this.nonce = tx.nonce;
		this.blockHash = tx.blockHash;
		this.blockNumber = tx.blockNumber;
		this.transactionIndex = tx.transactionIndex;
		this.from = tx.from;
		this.to = tx.to;
		this.value = tx.value;
		this.gasPrice = tx.gasPrice;
		this.gas = tx.gas;
		this.input = tx.input;
		this.v = tx.v;
		this.standardV = tx.standardV;
		this.r = tx.r;
		this.raw = tx.raw;
		this.publicKey = tx.publicKey;
		this.chainId = tx.chainId;
		this.creates = tx.creates;
		this.condition = tx.condition;
	}

	public String getHash() {
		return hash;
	}

	public String getNonce() {
		return nonce;
	}

	public String getBlockHash() {
		return blockHash;
	}

	public String getBlockNumber() {
		return blockNumber;
	}

	public String getTransactionIndex() {
		return transactionIndex;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getValue() {
		return value;
	}

	public String getGasPrice() {
		return gasPrice;
	}

	public String getGas() {
		return gas;
	}

	public String getInput() {
		return input;
	}

	public String getV() {
		return v;
	}

	public String getStandardV() {
		return standardV;
	}

	public String getR() {
		return r;
	}

	public String getRaw() {
		return raw;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public String getChainId() {
		return chainId;
	}

	public String getCreates() {
		return creates;
	}

	public String getCondition() {
		return condition;
	}
}
