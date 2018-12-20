package bdml.services.api.types;

public class Filter {
    private String id;
    private String idFrom;
    private String idTo;
    private int limit;

    public Filter(String id, String idFrom, String idTo, int limit) {
        this.id = id;
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.limit = limit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(String idFrom) {
        this.idFrom = idFrom;
    }

    public String getIdTo() {
        return idTo;
    }

    public void setIdTo(String idTo) {
        this.idTo = idTo;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
