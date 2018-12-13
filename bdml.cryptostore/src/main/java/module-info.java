module bdml.cryptostore {
	requires bdml.core;
	requires org.bouncycastle.provider;
	requires mapdb;
	requires gson;
	requires java.sql;
	
	exports bdml.cryptostore.mapping;

	provides bdml.core.service.CryptographicStore with bdml.cryptostore.CryptoStoreAdapter;
}