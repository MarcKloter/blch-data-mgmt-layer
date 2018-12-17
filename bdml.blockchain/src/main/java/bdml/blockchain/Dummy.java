package bdml.blockchain;

import java.io.IOException;
import java.security.GeneralSecurityException;

import bdml.blockchain.parity.Transaction;
import org.apache.http.client.ClientProtocolException;

import bdml.blockchain.jsonrpc.JsonRpc;
import bdml.blockchain.parity.SendTransaction;
import bdml.blockchain.parity.SendTransactionResponse;

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
		String txHash = "0x68d91d291b840c9c9bd6adb2379fbffe77214a800da46a2629069db659215e64";
		Transaction tx = bca.getTransactionByHash(txHash);
//		System.out.println(tx.getInput());
	}
}
