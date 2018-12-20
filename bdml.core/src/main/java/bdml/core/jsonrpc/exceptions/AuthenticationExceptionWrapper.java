package bdml.core.jsonrpc.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = -32000, message = "Account password is invalid or account does not exist.")
public class AuthenticationExceptionWrapper extends RuntimeException {
}
