module bdml.core {
	exports bdml.core.service;
	
	requires org.bouncycastle.provider;
	requires org.apache.httpcomponents.httpclient;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires javax.jws;
	requires javax.servlet.api;
	requires jsonrpc4j;
	requires portlet.api;
	requires spark.core;
	requires slf4j.api;
}