package bdml.services;

public class QueryResult<T> {
    public final T data;
    //if null means is still pending
    public final BlockTime inclusionTime;
    public final boolean plain;

    public QueryResult(T data, BlockTime inclusionTime, boolean plain) {
        this.data = data;
        this.inclusionTime = inclusionTime;
        this.plain = plain;
    }
}
