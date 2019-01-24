package bdml.api.jsonrpc.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = -32001)
public class NotAuthorizedExceptionWrapper extends RuntimeException {
    public NotAuthorizedExceptionWrapper(String message) {
        super(message);
    }
}
