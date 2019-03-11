package bdml.core.cache;

import bdml.core.cache.domain.Data;
import bdml.core.domain.Capability;
import bdml.core.domain.DataIdentifier;
import bdml.core.domain.TreeNode;
import bdml.services.helper.Account;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.exceptions.MissingConfigurationException;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

public class CacheImpl implements Cache {
    // mandatory configuration properties
    private static final String FALLBACK_BLOCK_KEY = "bdml.cache.fallback.block";
    private static final String OUTPUT_DIRECTORY_KEY = "bdml.output.directory";

    private static final String CIPHER = "AES";

    // fallback value if a cache file was deleted or corrupt, should be set to the block when the application was deployed
    private final String fallbackBlock;
    private final String outputDirectory;

    private final HashMap<Account, Connection> connections = new HashMap<>();

    public CacheImpl(Properties configuration) {
        // load configuration
        this.fallbackBlock = getProperty(configuration, FALLBACK_BLOCK_KEY);
        this.outputDirectory = getProperty(configuration, OUTPUT_DIRECTORY_KEY);
    }

    private String getProperty(Properties configuration, String property) {
        if (!configuration.containsKey(property))
            throw new MissingConfigurationException(property);

        return configuration.getProperty(property);
    }

    @Override
    public void initialize(Account account, String pointer) {
        try {
            Connection conn = createCache(account, pointer);
            conn.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Account has been initialized before.");
        }
    }

    @Override
    public void addCapability(Account account, Capability capability, boolean isAttachment) {
        Connection conn = getConnection(account);
        try {
            DataIdentifier identifier = capability.getIdentifier();
            Optional<Data> data = getData(conn, identifier);
            if (data.isEmpty()) {
                setData(conn, new Data(identifier.toByteArray(), capability.toByteArray(), isAttachment, false));
            } else {
                // only allow attachments to become non-attachments (if frame was never parsed directly before)
                if (isAttachment && !data.get().isAttachment())
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
    public Optional<Capability> getCapability(Account account, DataIdentifier identifier) {
        Connection conn = getConnection(account);
        try {
            Optional<Data> data = getData(conn, identifier);
            return data.map(Data::getCapability).map(Capability::new);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void setRecursivelyParsed(Account account, DataIdentifier identifier) {
        Connection conn = getConnection(account);
        try {
            Optional<Data> data = getData(conn, identifier);
            if (data.isEmpty())
                throw new IllegalArgumentException(String.format("There was no data found identified by '%s'", identifier.toString()));

            if (!data.get().wasRecursivelyParsed())
                setWasRecursivelyParsed(conn, identifier.toByteArray());
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public boolean wasRecursivelyParsed(Account account, DataIdentifier identifier) {
        Connection conn = getConnection(account);
        try {
            Optional<Data> data = getData(conn, identifier);
            return data.map(Data::wasRecursivelyParsed).orElse(false);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public Set<DataIdentifier> getAllIdentifiers(Account account, boolean includeAttachments) {
        String sql = "SELECT identifier FROM DATA" + (includeAttachments ? " WHERE attachment = FALSE" : "");
        Connection conn = getConnection(account);
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            Set<DataIdentifier> result = new HashSet<>();
            while (rs.next())
                result.add(new DataIdentifier(rs.getBytes("identifier")));

            return result;
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public String getPointer(Account account) {
        Connection conn = getConnection(account);
        try {
            return getVariable(conn, "pointer");
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void setPointer(Account account, String pointer) {
        Connection conn = getConnection(account);
        try {
            setVariable(conn, "pointer", pointer);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public String getPollPointer(Account account) {
        Connection conn = getConnection(account);
        try {
            return getVariable(conn, "poll-pointer");
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void setPollPointer(Account account, String pointer) {
        Connection conn = getConnection(account);
        try {
            setVariable(conn, "poll-pointer", pointer);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void addAttachment(Account account, DataIdentifier attachment, DataIdentifier attachedTo) {
        String sql = "MERGE INTO ATTACHMENTS KEY(identifier, attached_to) VALUES(?, ?)";
        Connection conn = getConnection(account);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, attachment.toByteArray());
            stmt.setBytes(2, attachedTo.toByteArray());
            stmt.executeUpdate();
        } catch (SQLException e) {
            switch (e.getErrorCode()) {
                case 23506: // PARENT_MISSING
                    return; // on-chain entry attached off-chain
                case 22001: // VALUE_TOO_LONG
                case 23502: // NULL_NOT_ALLOWED
                    throw new IllegalArgumentException(e.getMessage());
                default:
                    throw new MisconfigurationException(e.getMessage());
            }
        }
    }

    @Override
    public TreeNode<DataIdentifier> getAllAttachments(Account account, DataIdentifier identifier) {
        Connection conn = getConnection(account);
        try {
            return getAttachments(conn, identifier);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void release(Account account) {
        if(this.connections.containsKey(account)) {
            try {
                this.connections.get(account).close();
            } catch (SQLException e) {
                throw new MisconfigurationException(e.getMessage());
            }
            this.connections.remove(account);
        }
    }

    private Optional<Data> getData(Connection conn, DataIdentifier identifier) throws SQLException {
        String sql = "SELECT * FROM DATA WHERE identifier = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier.toByteArray());
            try (ResultSet rs = stmt.executeQuery()) {
                return Data.buildFrom(rs);
            }
        }
    }

    private TreeNode<DataIdentifier> getAttachments(Connection conn, DataIdentifier identifier) throws SQLException {
        String sql = "SELECT * FROM ATTACHMENTS WHERE attached_to = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier.toByteArray());
            try (ResultSet rs = stmt.executeQuery()) {
                Set<TreeNode<DataIdentifier>> attachments = new HashSet<>();
                while (rs.next()) {
                    DataIdentifier attachment = new DataIdentifier(rs.getBytes("identifier"));
                    attachments.add(getAttachments(conn, attachment));
                }
                return new TreeNode<>(identifier, attachments);
            }
        }
    }

    private void setData(Connection conn, Data data) throws SQLException {
        String sql = "INSERT INTO DATA(identifier, capability, attachment) VALUES(?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, data.getIdentifier());
            stmt.setBytes(2, data.getCapability());
            stmt.setBoolean(3, data.isAttachment());
            stmt.executeUpdate();
        }
    }

    private void setIsAttachment(Connection conn, DataIdentifier identifier) throws SQLException {
        String sql = "UPDATE DATA SET attachment = ? WHERE identifier = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, true);
            stmt.setBytes(2, identifier.toByteArray());
            stmt.executeUpdate();
        }
    }

    private void setWasRecursivelyParsed(Connection conn, byte[] identifier) throws SQLException {
        String sql = "UPDATE DATA SET recursively_parsed = ? WHERE identifier = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, true);
            stmt.setBytes(2, identifier);
            stmt.executeUpdate();
        }
    }

    private String getVariable(Connection conn, String variable) throws SQLException {
        String sql = "SELECT value FROM VARIABLES WHERE key = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, variable);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getString("value");
            }
        }
    }

    private void setVariable(Connection conn, String variable, String value) throws SQLException {
        String sql = "UPDATE VARIABLES SET value = ? WHERE key = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, variable);
            stmt.executeUpdate();
        }
    }

    private Connection getConnection(Account account) {
        // check whether the account has an open connection
        if(this.connections.containsKey(account.getIdentifier()))
            return connections.get(account.getIdentifier());

        Connection conn;
        try {
            // only allow to open existing databases
            conn = connect(account, ";IFEXISTS=TRUE");
        } catch (SQLException e1) {
            try {
                // create an empty cache with a pointer to null
                conn = createCache(account, fallbackBlock);
            } catch (SQLException e2) {
                throw new MisconfigurationException(e2.getMessage());
            }
        }
        connections.put(account, conn);
        return conn;
    }

    private Connection connect(Account account, String params) throws SQLException {
        String db = account.getIdentifier();
        // combination of file password (used for .db file encryption) and user password
        String pwd = String.format("%s-%s %<s", db, account.getPassword());

        // using h2 in auto mixed mode
        String url = String.format("jdbc:h2:./%s/%s;CIPHER=%s;AUTO_SERVER=TRUE%s", outputDirectory, db, CIPHER, params);
        return DriverManager.getConnection(url, db, pwd);
    }

    private Connection createCache(Account account, String pointer) throws SQLException {
        // only allow the creation of non-existent databases
        Connection conn = connect(account, ";IFEXISTS=FALSE");

        // create schema
        RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream("/schema.sql")));

        // initialize pointers to the blocks of the connected blockchain
        String sql = "INSERT INTO VARIABLES(key, value) VALUES(?, ?), (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "pointer");
            stmt.setString(2, pointer);
            stmt.setString(3, "poll-pointer");
            stmt.setString(4, pointer);
            stmt.executeUpdate();
        }

        return conn;
    }
}
