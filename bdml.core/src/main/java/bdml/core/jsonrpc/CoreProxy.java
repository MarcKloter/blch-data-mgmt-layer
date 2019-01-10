package bdml.core.jsonrpc;

import bdml.core.CoreService;
import bdml.core.jsonrpc.exceptions.AuthenticationExceptionWrapper;
import bdml.core.jsonrpc.exceptions.InvalidParamsException;
import bdml.core.jsonrpc.exceptions.NotAuthorizedExceptionWrapper;
import bdml.core.jsonrpc.types.AccountWrapper;
import bdml.services.api.Core;
import bdml.services.api.exceptions.AuthenticationException;
import bdml.services.api.exceptions.NotAuthorizedException;
import bdml.services.api.types.Data;
import bdml.services.api.types.Identifier;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@JsonRpcService
public class CoreProxy {
    private Core core = new CoreService();

    @JsonRpcMethod
    public String storeData(@JsonRpcParam("data") String data,
                            @JsonRpcParam("attachments") @JsonRpcOptional List<String> attachments,
                            @JsonRpcParam("account") AccountWrapper account,
                            @JsonRpcParam("subjects") List<String> subjects) {
        try {
            return core.storeData(data, attachments, account.unwrap(), subjects);
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException(e.getMessage());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedExceptionWrapper(e.getMessage());
        }
    }

    @JsonRpcMethod
    public Set<String> listData(@JsonRpcParam("account") AccountWrapper account) {
        try {
            return core.listData(account.unwrap());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public Set<String> listDataChanges(@JsonRpcParam("account") AccountWrapper account) {
        try {
            return core.listDataChanges(account.unwrap());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public Identifier listAttachments(@JsonRpcParam("account") AccountWrapper account,
                                      @JsonRpcParam("identifier") String identifier) {
        try {
            return core.listAttachments(account.unwrap(), identifier);
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException(e.getMessage());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedExceptionWrapper(e.getMessage());
        }
    }

    @JsonRpcMethod
    public Data getData(@JsonRpcParam("id") String id,
                           @JsonRpcParam("account") AccountWrapper account) {
        try {
            return core.getData(id, account.unwrap());
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException(e.getMessage());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedExceptionWrapper(e.getMessage());
        }
    }

    @JsonRpcMethod
    public String createAccount(@JsonRpcParam("password") String password) {
        try {
            return core.createAccount(password);
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException(e.getMessage());
        }
    }
}
