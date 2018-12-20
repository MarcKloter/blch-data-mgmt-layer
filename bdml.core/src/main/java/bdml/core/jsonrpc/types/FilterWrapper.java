package bdml.core.jsonrpc.types;

import bdml.services.api.types.Filter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;

public class FilterWrapper {
    private Filter filter;

    @JsonCreator
    public FilterWrapper(@JsonProperty("id") @JsonRpcOptional String id,
                         @JsonProperty("idFrom") @JsonRpcOptional String idFrom,
                         @JsonProperty("idTo") @JsonRpcOptional String idTo,
                         @JsonProperty("limit") @JsonRpcOptional int limit) {
        this.filter = new Filter(id, idFrom, idTo, limit);
    }

    public Filter unwrap() {
        return filter;
    }
}
