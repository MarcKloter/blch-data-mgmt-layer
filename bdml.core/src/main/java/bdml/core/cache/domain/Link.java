package bdml.core.cache.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Link {
    private byte[] source;
    private byte[] target;
    private boolean amend;

    public Link(byte[] source, byte[] target, boolean amend) {
        this.source = source;
        this.target = target;
        this.amend = amend;
    }

    public static Optional<Link> buildFrom(ResultSet rs) throws SQLException {
        if(rs.next()) {
            byte[] source = rs.getBytes("source");
            byte[] target = rs.getBytes("target");
            boolean amend = rs.getBoolean("amend");

            return Optional.of(new Link(source, target, amend));
        } else {
            return Optional.empty();
        }
    }

    public byte[] getSource() {
        return source;
    }

    public byte[] getTarget() {
        return target;
    }

    public boolean isAmend() {
        return amend;
    }
}
