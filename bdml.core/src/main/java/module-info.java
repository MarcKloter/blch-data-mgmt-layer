module bdml.core {
	requires bdml.services;
	
	uses bdml.services.Cache;

	requires org.bouncycastle.provider;
	requires org.apache.httpcomponents.httpclient;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires javax.jws;
	requires javax.servlet.api;
	requires jsonrpc4j;
	requires spark.core;
}