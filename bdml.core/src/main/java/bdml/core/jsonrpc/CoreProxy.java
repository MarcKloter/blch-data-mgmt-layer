package bdml.core.jsonrpc;

import bdml.core.CoreService;
import bdml.core.domain.*;
import bdml.core.jsonrpc.exceptions.AuthenticationExceptionWrapper;
import bdml.core.jsonrpc.exceptions.InvalidParamsException;
import bdml.core.jsonrpc.exceptions.NotAuthorizedExceptionWrapper;
import bdml.core.jsonrpc.types.AccountWrapper;
import bdml.core.Core;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.exceptions.NotAuthorizedException;
import bdml.core.jsonrpc.types.GetDataResponse;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;

import java.util.Set;
import java.util.stream.Collectors;

@JsonRpcService
public class CoreProxy {
    private Core core = CoreService.getInstance();

    @JsonRpcMethod
    public String storeData(@JsonRpcParam("data") String data,
                            @JsonRpcParam("attachments") @JsonRpcOptional Set<String> attachments,
                            @JsonRpcParam("account") AccountWrapper account,
                            @JsonRpcParam("subjects") Set<String> subjects) {
        try {
            Set<DataIdentifier> mappedAttachments = attachments.stream().map(DataIdentifier::decode).collect(Collectors.toSet());
            Set<Subject> mappedSubjects = subjects.stream().map(Subject::decode).collect(Collectors.toSet());
            return core.storeData(new Data(data, mappedAttachments), account.unwrap(), mappedSubjects).toString();
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
            return core.listData(account.unwrap()).stream().map(DataIdentifier::toString).collect(Collectors.toSet());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public Set<String> listDataChanges(@JsonRpcParam("account") AccountWrapper account) {
        try {
            return core.listDataChanges(account.unwrap()).stream().map(DataIdentifier::toString).collect(Collectors.toSet());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public TreeNode<DataIdentifier> listAttachments(@JsonRpcParam("id") String identifier,
                                                    @JsonRpcParam("account") AccountWrapper account) {
        try {
            return core.listAttachments(DataIdentifier.decode(identifier), account.unwrap());
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException(e.getMessage());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedExceptionWrapper(e.getMessage());
        }
    }

    @JsonRpcMethod
    public GetDataResponse getData(@JsonRpcParam("id") String id,
                                   @JsonRpcParam("account") AccountWrapper account) {
        try {
            Data data = core.getData(DataIdentifier.decode(id), account.unwrap());
            Set<String> attachments = data.getAttachments().stream().map(DataIdentifier::toString).collect(Collectors.toSet());
            return new GetDataResponse(data.getData(), attachments);
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
            return core.createAccount(password).toString();
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException(e.getMessage());
        }
    }
}
