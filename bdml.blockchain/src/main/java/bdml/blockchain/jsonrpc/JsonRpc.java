package bdml.blockchain.jsonrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import com.google.gson.Gson;

public class JsonRpc {
	/**
	 * Sends the given JSON-RPC request to the provided URI.
	 * 
	 * @param uri
	 *            JSON-RPC API endpoint URI
	 * @param request
	 *            object containing JSON-RPC request information
	 * @param classOfResponse
	 *            class to deserialize the response into
	 * @return an object of class T containing the JSON-RPC response
	 * @throws ClientProtocolException
	 *             in case of an HTTP protocol error
	 * @throws IOException
	 *             if there was an error parsing the response
	 */
	public static <T extends JsonRpcResponse> T send(String uri, JsonRpcRequest request, Class<T> classOfResponse)
			throws ClientProtocolException, IOException {
		// construct JSON RPC data
		Gson gson = new Gson();
		String json = gson.toJson(request);
		StringEntity data = new StringEntity(json);

		// execute HTTP request
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(uri);
		post.setEntity(data);
		post.setHeader("Content-type", "application/json");
		HttpResponse httpResponse = httpClient.execute(post);

		// parse HTTP response
		InputStream source = httpResponse.getEntity().getContent();
		InputStreamReader reader = new InputStreamReader(source);

		// deserialize JSON RPC response
		T response = gson.fromJson(reader, classOfResponse);
		return response;
	}
	
	/**
	 * Creates a JSON-RPC request id.
	 * 
	 * @return valid JSON-RPC request id
	 */
	private static long id = 0;
	public static String getId() {
		id = (id + 1) & 0xffffffffl; // unsigned value
		return Long.toString(id); 
	}
}