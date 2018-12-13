package bdml.blockchain.jsonrpc;

public abstract class JsonRpcRequest {
	public String jsonrpc = "2.0";
	public String id;
	public String method;
	
	public JsonRpcRequest(String id, String method) {
		this.id = id;
		this.method = method;
	}
}