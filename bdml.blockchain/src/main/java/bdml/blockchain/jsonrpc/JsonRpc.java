package bdml.blockchain.jsonrpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class JsonRpc {
    private static long id = 0;

    /**
     * Sends the given JSON-RPC request to the provided URI.
     *
     * @param uri             JSON-RPC API endpoint URI
     * @param request         object containing JSON-RPC request information
     * @param classOfResponse class to deserialize the response into
     * @return Object of class T containing the JSON-RPC response.
     */
    public static <T extends JsonRpcResponse> T send(String uri, JsonRpcRequest request, Class<T> classOfResponse) {
        // construct JSON RPC data
        String requestObject = serializeRequest(request);

        // TODO: Logger
        System.out.println("--> " + requestObject);

        String responseObject = executeJsonRpcRequest(uri, requestObject);

        // TODO: Logger
        System.out.println("<-- " + responseObject);

        // deserialize JSON RPC response
        return deserializeResponse(responseObject, classOfResponse);
    }

    private static String serializeRequest(JsonRpcRequest request) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(request);
        } catch(JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static <T extends JsonRpcResponse> T deserializeResponse(String response, Class<T> classOfResponse) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(response, classOfResponse);
        } catch(IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String executeJsonRpcRequest(String uri, String requestObject) {
        // execute HTTP request
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(uri);
        post.setHeader("Content-type", "application/json");
        try {
            StringEntity data = new StringEntity(requestObject);
            post.setEntity(data);
            HttpResponse httpResponse = httpClient.execute(post);

            // parse HTTP response
            InputStream source = httpResponse.getEntity().getContent();
            return new String(source.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Creates a JSON-RPC request id.
     *
     * @return valid JSON-RPC request id
     */
    public static String getId() {
        id = (id + 1) & 0xffffffffl; // unsigned value
        return Long.toString(id);
    }
}
