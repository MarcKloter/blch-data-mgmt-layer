package bdml.core.jsonrpc;

import bdml.core.CoreService;
import bdml.core.jsonrpc.exceptions.AuthenticationExceptionWrapper;
import bdml.core.jsonrpc.exceptions.InvalidParamsException;
import bdml.core.jsonrpc.types.AccountWrapper;
import bdml.core.jsonrpc.types.DataSkeleton;
import bdml.core.jsonrpc.types.FilterWrapper;
import bdml.services.api.Core;
import bdml.services.api.exceptions.AuthenticationException;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;

import java.util.List;

@JsonRpcService
public class CoreProxy {
    private Core core = new CoreService();

    @JsonRpcMethod
    public String storeData(@JsonRpcParam("data") String data,
                            @JsonRpcParam("attachments") @JsonRpcOptional List<String> attachments,
                            @JsonRpcParam("account") AccountWrapper account,
                            @JsonRpcParam("subjects") List<String> subjects) {
        // TODO: write additional message to data object, currently overwriting the message field
        try {
            return core.storeData(data, attachments, account.unwrap(), subjects);
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException(e.getMessage());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public List<String> listData(@JsonRpcParam("account") AccountWrapper account,
                                 @JsonRpcParam("filter") @JsonRpcOptional FilterWrapper filter) {
        try {
            return core.listData(account.unwrap(), filter != null ? filter.unwrap() : null);
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public DataSkeleton getData(@JsonRpcParam("id") String id,
                                @JsonRpcParam("account") AccountWrapper account) {
        try {
            return new DataSkeleton(core.getData(id, account.unwrap()));
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public List<String> listSubjects() {
        return core.listSubjects();
    }

    @JsonRpcMethod
    public String createAccount(@JsonRpcParam("password") String password) {
        return core.createAccount(password);
    }
}
