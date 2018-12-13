module bdml.cache {
	requires bdml.core;
	requires h2;
	requires java.sql;
	
	exports bdml.cache;
	
	provides bdml.core.service.Cache with bdml.cache.CacheImpl;
}