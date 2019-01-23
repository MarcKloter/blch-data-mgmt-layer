package bdml.api.jsonrpc;

import bdml.api.jsonrpc.types.AccountWrapper;
import bdml.core.CoreService;
import bdml.core.domain.*;
import bdml.api.jsonrpc.exceptions.AuthenticationExceptionWrapper;
import bdml.api.jsonrpc.exceptions.InvalidParamsException;
import bdml.api.jsonrpc.exceptions.NotAuthorizedExceptionWrapper;
import bdml.core.Core;
import bdml.core.domain.exceptions.AuthenticationException;
import bdml.core.domain.exceptions.NotAuthorizedException;
import bdml.api.jsonrpc.types.GetDataResponse;
import bdml.api.jsonrpc.types.ListAttachmentsResponse;
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
            return core.listDirectlyAccessibleData(account.unwrap()).stream().map(DataIdentifier::toString).collect(Collectors.toSet());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public Set<String> listDataChanges(@JsonRpcParam("account") AccountWrapper account) {
        try {
            return core.listDirectlyAccessibleDataChanges(account.unwrap()).stream().map(DataIdentifier::toString).collect(Collectors.toSet());
        } catch (AuthenticationException e) {
            throw new AuthenticationExceptionWrapper();
        }
    }

    @JsonRpcMethod
    public ListAttachmentsResponse listAttachments(@JsonRpcParam("id") String identifier,
                                                   @JsonRpcParam("account") AccountWrapper account) {
        try {
            return new ListAttachmentsResponse(core.listAttachments(DataIdentifier.decode(identifier), account.unwrap()));
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
            return GetDataResponse.of(core.getData(DataIdentifier.decode(id), account.unwrap()));
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
