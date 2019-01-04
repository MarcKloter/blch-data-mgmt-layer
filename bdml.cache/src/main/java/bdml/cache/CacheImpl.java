package bdml.cache;

import bdml.services.Cache;
import bdml.services.api.types.Account;
import bdml.services.exceptions.MisconfigurationException;
import java.sql.*;

public class CacheImpl implements Cache {
    // TODO: load constants from configuration file
    private final String CIPHER = "AES";

    @Override
    public void add(Account account, byte[] id, byte[] capability) {
        String sql = "INSERT INTO CAPABILITIES(id, capability) VALUES(?, ?)";
        try(Connection conn = connect(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, id);
            stmt.setBytes(2, capability);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            switch (e.getErrorCode()) {
                case 23505: // DUPLICATE_KEY
                    // TODO: capability was put before
                    break;
                case 22001: // VALUE_TOO_LONG
                case 23502: // NULL_NOT_ALLOWED
                    // TODO: invalid argument exception
                    break;
                default:

            }
            // TODO: switch by error code (eg invalid input) e.getErrorCode()
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public byte[] get(Account account, byte[] id) {
        String sql = "SELECT capability FROM CAPABILITIES WHERE id = ?";
        try(Connection conn = connect(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, id);

            try(ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBytes("capability") : null;
            }
        } catch (SQLException e) {
            // TODO: switch by error code e.getErrorCode()
            throw new RuntimeException(e.getMessage());
        }
    }

    private Connection connect(Account account) {
        String db = account.getIdentifier();
        String pwd = account.getPassword();

        // combination of file password (used for encryption) and user password
        String password = Util.sha256(db + pwd) + " " + pwd;

        // only allow to open existing databases
        String url = String.format("jdbc:h2:./%s;CIPHER=%s;IFEXISTS=TRUE", db, CIPHER);
        try {
            return DriverManager.getConnection(url, db, password);
        } catch (SQLException e) {
            return createCache(db, password);
        }
    }

    private Connection createCache(String db, String password) {
        String url = String.format("jdbc:h2:./%s;CIPHER=%s;IFEXISTS=FALSE", db, CIPHER);
        try {
            Connection conn = DriverManager.getConnection(url, db, password);
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE CAPABILITIES(id BINARY(32) primary key, capability BINARY(32) NOT NULL)");
            return conn;
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }
}
