module bdml.blockchain {
	requires bdml.core;
	requires org.bouncycastle.provider;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
	requires gson;
	requires java.sql;
	
	exports blockchain.parity;
	exports bdml.blockchain.jsonrpc;
	
	provides bdml.core.service.Blockchain with bdml.blockchain.BlockchainAdapter;
}