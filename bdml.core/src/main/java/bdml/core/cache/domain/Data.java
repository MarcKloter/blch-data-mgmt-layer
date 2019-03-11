package bdml.core.cache.domain;

import bdml.services.BlockTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Data {
    private byte[] identifier;
    private byte[] capability;
    private boolean temporary;

    public Data(byte[] identifier, byte[] capability, boolean temporary) {
        this.identifier = identifier;
        this.capability = capability;
        this.temporary = temporary;
    }

    public static Optional<Data> buildFrom(ResultSet rs) throws SQLException {
        if(rs.next()) {
            byte[] identifier = rs.getBytes("identifier");
            byte[] capability = rs.getBytes("capability");
            boolean temporary = rs.getBoolean("temporary");

            return Optional.of(new Data(identifier, capability, temporary));
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

    public boolean isTemporary() {
        return temporary;
    }
}
