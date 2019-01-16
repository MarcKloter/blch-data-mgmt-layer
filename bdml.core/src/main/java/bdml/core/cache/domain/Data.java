package bdml.core.cache.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Data {
    private byte[] identifier;
    private byte[] capability;
    private boolean isAttachment;
    private boolean recursivelyParsed;

    public Data(byte[] identifier, byte[] capability, boolean isAttachment, boolean recursivelyParsed) {
        this.identifier = identifier;
        this.capability = capability;
        this.isAttachment = isAttachment;
        this.recursivelyParsed = recursivelyParsed;
    }

    public static Optional<Data> buildFrom(ResultSet rs) throws SQLException {
        if(rs.next()) {
            byte[] identifier = rs.getBytes("identifier");
            byte[] capability = rs.getBytes("capability");
            boolean isAttachment = rs.getBoolean("attachment");
            boolean recursivelyParsed = rs.getBoolean("recursively_parsed");
            return Optional.of(new Data(identifier, capability, isAttachment, recursivelyParsed));
        } else {
            return Optional.empty();
        }
    }

    public byte[] getIdentifier() {
        return identifier;
    }

    public byte[] getCapability() {
        return capability;
    }

    public boolean isAttachment() {
        return isAttachment;
    }

    public boolean wasRecursivelyParsed() {
        return recursivelyParsed;
    }
}
