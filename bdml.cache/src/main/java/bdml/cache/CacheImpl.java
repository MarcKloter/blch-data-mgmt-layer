package bdml.cache;

import bdml.cache.domain.Data;
import bdml.services.Cache;
import bdml.services.api.types.Account;
import bdml.services.api.types.Identifier;
import bdml.services.exceptions.MisconfigurationException;
import org.apache.commons.codec.binary.Hex;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CacheImpl implements Cache {
    // TODO: load constants from configuration file
    private final String CIPHER = "AES";
    private final String DIRECTORY = "bdml-data";
    private final int POINTER_INDEX = 1;

    @Override
    public void initialize(Account account, byte[] pointer) {
        try {
            createCache(account, pointer);
        } catch (SQLException e) {
            throw new IllegalStateException("Account has been initialized before.");
        }
    }

    @Override
    public void addCapability(Account account, byte[] identifier, byte[] capability, boolean isAttachment) {
        try(Connection conn = connectOrCreate(account)) {
            Optional<Data> data = getData(conn, identifier);
            if(data.isEmpty()) {
                setData(conn, new Data(identifier, capability, isAttachment, false));
            } else {
                // only allow attachments to become non-attachments (if frame was never parsed directly before)
                if(isAttachment && !data.get().isAttachment())
                    setIsAttachment(conn, identifier);
            }
        } catch (SQLException e) {
            switch (e.getErrorCode()) {
                case 22001: // VALUE_TOO_LONG
                case 23502: // NULL_NOT_ALLOWED
                    throw new IllegalArgumentException(e.getMessage());
                default:
                    throw new MisconfigurationException(e.getMessage());
            }
        }
    }

    @Override
    public byte[] getCapability(Account account, byte[] identifier) {
        try(Connection conn = connectOrCreate(account)) {
            Optional<Data> data = getData(conn, identifier);
            return data.map(Data::getCapability).orElse(null);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void setRecursivelyParsed(Account account, byte[] identifier) {
        try(Connection conn = connectOrCreate(account)) {
            Optional<Data> data = getData(conn, identifier);
            if(data.isEmpty())
                throw new IllegalArgumentException(String.format("There was no data found identified by '%s'", Hex.encodeHexString(identifier)));

            if(!data.get().wasRecursivelyParsed())
                setWasRecursivelyParsed(conn, identifier);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public boolean wasRecursivelyParsed(Account account, byte[] identifier) {
        try(Connection conn = connectOrCreate(account)) {
            Optional<Data> data = getData(conn, identifier);
            return data.map(Data::wasRecursivelyParsed).orElse(false);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public Set<byte[]> getDirectlyAccessibleIdentifiers(Account account) {
        String sql = "SELECT identifier FROM DATA";
        try(Connection conn = connectOrCreate(account); ResultSet rs = conn.createStatement().executeQuery(sql)) {
            Set<byte[]> result = new HashSet<>();
            while(rs.next())
                result.add(rs.getBytes("identifier"));

            return result;
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public byte[] getPointer(Account account) {
        String sql = "SELECT identifier FROM PARSED WHERE row_index = ?";
        try(Connection conn = connectOrCreate(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, POINTER_INDEX);
            try (ResultSet rs = stmt.executeQuery()) {
                return (rs.next()) ? rs.getBytes("identifier") : null;
            }
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void movePointer(Account account, byte[] identifier) {
        String sql = "UPDATE PARSED SET identifier = ? WHERE row_index = ?";
        try(Connection conn = connectOrCreate(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier);
            stmt.setInt(2, POINTER_INDEX);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void markAsParsed(Account account, byte[] identifier) {
        if(wasParsedBefore(account, identifier))
            return;

        String sql = "INSERT INTO PARSED(identifier) VALUES(?)";
        try(Connection conn = connectOrCreate(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public boolean wasParsedBefore(Account account, byte[] identifier) {
        String sql = "SELECT identifier FROM PARSED WHERE identifier = ?";
        try(Connection conn = connectOrCreate(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void addAttachment(Account account, byte[] identifier, byte[] attachedTo) {
        String sql = "MERGE INTO ATTACHMENTS KEY(identifier, attached_to) VALUES(?, ?)";
        try(Connection conn = connectOrCreate(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier);
            stmt.setBytes(2, attachedTo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public Identifier getAllAttachments(Account account, byte[] identifier) {
        String sql = "SELECT * FROM ATTACHMENTS WHERE attached_to = ?";
        try(Connection conn = connectOrCreate(account); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier);
            try (ResultSet rs = stmt.executeQuery()) {
                Set<Identifier> attachments = new HashSet<>();
                while(rs.next()) {
                    byte[] attachment = rs.getBytes("identifier");
                    attachments.add(getAllAttachments(account, attachment));
                }

                return new Identifier(Hex.encodeHexString(identifier), attachments);
            }
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    private Optional<Data> getData(Connection conn, byte[] identifier) throws SQLException {
        String sql = "SELECT * FROM DATA WHERE identifier = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBytes(1, identifier);
        try(ResultSet rs = stmt.executeQuery()) {
            return Data.buildFrom(rs);
        }
    }

    private void setData(Connection conn, Data data) throws SQLException {
        String sql = "INSERT INTO DATA(identifier, capability, attachment) VALUES(?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBytes(1, data.getIdentifier());
        stmt.setBytes(2, data.getCapability());
        stmt.setBoolean(3, data.isAttachment());
        stmt.executeUpdate();
    }

    private void setIsAttachment(Connection conn, byte[] identifier) throws SQLException {
        String sql = "UPDATE DATA SET attachment = ? WHERE identifier = ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBoolean(1, true);
        stmt.setBytes(2, identifier);
        stmt.executeUpdate();
    }

    private void setWasRecursivelyParsed(Connection conn, byte[] identifier) throws SQLException {
        String sql = "UPDATE DATA SET recursively_parsed = ? WHERE identifier = ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBoolean(1, true);
        stmt.setBytes(2, identifier);
        stmt.executeUpdate();
    }

    private Connection connectOrCreate(Account account) {
        try {
            // only allow to open existing databases
            return connect(account, ";IFEXISTS=TRUE");
        } catch (SQLException e1) {
            try {
                // create an empty cache with a pointer to null
                return createCache(account, null);
            } catch (SQLException e2) {
                throw new MisconfigurationException(e2.getMessage());
            }
        }
    }

    private Connection connect(Account account, String params) throws SQLException {
        String db = account.getIdentifier();
        String pwd = account.getPassword();

        // combination of file password (used for encryption) and user password
        String password = Util.sha256(db + pwd) + " " + pwd;

        String url = String.format("jdbc:h2:./%s/%s;CIPHER=%s", DIRECTORY, db, CIPHER);

        return DriverManager.getConnection(url + params, db, password);
    }

    private Connection createCache(Account account, byte[] pointer) throws SQLException {
        // only allow the creation of non-existent databases
        Connection conn = connect(account, ";IFEXISTS=FALSE");

        // create schema
        RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream("/schema.sql")));

        // set index = 0 to the pointer
        String sql = "INSERT INTO PARSED(identifier) VALUES(?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBytes(1, pointer);
        stmt.executeUpdate();

        return conn;
    }
}
