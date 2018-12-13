package bdml.blockchain;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.apache.http.client.ClientProtocolException;

import bdml.blockchain.jsonrpc.JsonRpc;
import blockchain.parity.GetTransactionByHash;
import blockchain.parity.GetTransactionByHashResponse;
import blockchain.parity.NewAccount;
import blockchain.parity.NewAccountResponse;
import blockchain.parity.SendTransaction;
import blockchain.parity.SendTransactionResponse;

public class Dummy {
	public static void main(String[] args) throws GeneralSecurityException, ClientProtocolException, IOException {
		String uri = "http://localhost:8545";
		String password = "myPassword";
		
		BlockchainAdapter bca = new BlockchainAdapter(uri);
		
		String address = bca.createAccount(password);
		System.out.println("Account created: " + address);
		
//		String address = "0x0f5616d9b0bd843eba300d397ded76d4413c5c27";
		SendTransaction request = new SendTransaction(JsonRpc.getId(), address, "0x132e", password);
		SendTransactionResponse response = JsonRpc.send(uri, request, SendTransactionResponse.class);
		System.out.println(response.result);
		
//		String txHash = "0x3291c9e124e39f5d2c1dc969f56cad3a816dc9059949db15e7127cb799ba575f";
		String txHash = "0x5485bdf0e66e7a26adab019854a9960022134f62df15d7b76aaca955a503ec8e";
		Transaction tx = bca.getTransactionByHash(txHash);
//		System.out.println(tx.getInput());
	}
}
