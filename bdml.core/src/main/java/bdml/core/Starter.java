package bdml.core;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import static spark.Spark.*;

public class Starter {
	public static void main(String[] args) {
		CoreService coreService = new CoreServiceImpl();
		JsonRpcServer jsonRpcServer = new JsonRpcServer(coreService);
		
		int port = 8545;
		port(port);
		
		// HTTPS POST routing
        post("/", (request, response) -> {
        	jsonRpcServer.handle(request.raw(), response.raw());
        	return response;
        });
        
		System.out.println("JSON-RPC API endpoint listening on https://localhost:" + port);
	}
}
