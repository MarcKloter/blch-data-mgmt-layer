package bdml.core.cache.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Link {
    private byte[] source;
    private byte[] target;
    private boolean amend;
    private boolean temporary;


    public Link(byte[] source, byte[] target, boolean amend, boolean temporary) {
        this.source = source;
        this.target = target;
        this.amend = amend;
        this.temporary = temporary;
    }

    public static Set<Link> buildAllFrom(ResultSet rs) throws SQLException {
        Set<Link> result = new HashSet<>();
        while (rs.next()) {
            result.add(buildCurrentFrom(rs));
        }
        return result;
    }

    private static Link buildCurrentFrom(ResultSet rs) throws SQLException {
            byte[] source = rs.getBytes("source");
            byte[] target = rs.getBytes("target");
            boolean amend = rs.getBoolean("amend");
            boolean temporary = rs.getBoolean("temporary");
            return new Link(source, target, amend, temporary);
    }

    public static Optional<Link> buildFrom(ResultSet rs) throws SQLException {
        if(rs.next()) {
            return Optional.of(buildCurrentFrom(rs));
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

    public boolean isTemporary() {
        return temporary;
    }
}
