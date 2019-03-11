package bdml.core.domain.exceptions;

import bdml.core.domain.DataIdentifier;

public class DataUnavailableException extends Exception {
    public DataUnavailableException(DataIdentifier id) {
        super("Data with id:"+id.toString()+" is not accessible");
    }
}
