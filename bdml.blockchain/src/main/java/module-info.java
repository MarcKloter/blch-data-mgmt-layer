module bdml.blockchain {
	requires bdml.services;
	
	exports blockchain.parity;
	exports bdml.blockchain.jsonrpc;
	
	provides bdml.services.Blockchain with bdml.blockchain.BlockchainAdapter;

	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
	requires gson;
	requires java.sql;
}