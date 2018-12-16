module bdml.cryptostore {
	requires bdml.services;
	
	exports bdml.cryptostore.mapping;

	provides bdml.services.CryptographicStore with bdml.cryptostore.CryptoStoreAdapter;
	
	requires org.bouncycastle.provider;
	requires mapdb;
	requires gson;
	requires java.sql;
}