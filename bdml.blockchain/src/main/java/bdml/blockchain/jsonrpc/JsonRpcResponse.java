package bdml.blockchain.jsonrpc;

public abstract class JsonRpcResponse {
	public String jsonrpc;
	public String id;
	public JsonRpcError error;
}