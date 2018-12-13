package blockchain.parity;

import bdml.blockchain.jsonrpc.JsonRpcRequest;

public class NewAccount extends JsonRpcRequest {
	public String[] params;

	/**
	 * https://wiki.parity.io/JSONRPC-personal-module#personal_newaccount
	 */
	public NewAccount(String id, String password) {
		super(id, "personal_newAccount");
		this.params = new String[] { password };
	}
}
