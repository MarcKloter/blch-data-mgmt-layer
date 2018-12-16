module bdml.cache {
	requires bdml.services;
	
	exports bdml.cache;
	
	provides bdml.services.Cache with bdml.cache.CacheImpl;

	requires h2;
	requires java.sql;
}