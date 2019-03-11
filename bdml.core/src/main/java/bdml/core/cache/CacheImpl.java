package bdml.core.cache;

import bdml.core.cache.domain.Data;
import bdml.core.cache.domain.Link;
import bdml.core.domain.Capability;
import bdml.core.domain.DataIdentifier;
import bdml.services.helper.Account;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.exceptions.MissingConfigurationException;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

public class CacheImpl implements PersonalCache {
    // mandatory configuration properties
    private static final String FALLBACK_BLOCK_KEY = "bdml.cache.fallback.block";
    private static final String OUTPUT_DIRECTORY_KEY = "bdml.output.directory";
    private static final String CIPHER = "AES";

    private final Account account;
    private final Connection conn;

    // fallback value if a cache file was deleted or corrupt, should be set to the block when the application was deployed
    private final long fallbackBlock;
    private final String outputDirectory;

    public CacheImpl(Account account, Properties configuration) {
        // load configuration
        this.fallbackBlock = Long.parseLong(getProperty(configuration, FALLBACK_BLOCK_KEY));
        this.outputDirectory = getProperty(configuration, OUTPUT_DIRECTORY_KEY);
        this.account = account;
        this.conn = connectOrCreate();
    }

    private String getProperty(Properties configuration, String property) {
        if (!configuration.containsKey(property))
            throw new MissingConfigurationException(property);

        return configuration.getProperty(property);
    }

    @Override
    public void addCapability(Capability capability, boolean temporary) {
        try {
            DataIdentifier identifier = capability.getIdentifier();
            Optional<Data> data = getData(identifier);
            if (data.isEmpty()) {
                setData(new Data(identifier.toByteArray(), capability.toByteArray(), temporary));
                if(!temporary){
                    addToLog(identifier);
                }
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
    public void addLink(DataIdentifier source, DataIdentifier target, boolean isAmend) {
        try {
            Link link = new Link(source.toByteArray(), target.toByteArray(), isAmend);
            if (!hasLink(link)) {
                setLink(link);
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
    public Optional<Capability> getCapability(DataIdentifier identifier) {
        try {
            Optional<Data> data = getData(identifier);
            return data.map(Data::getCapability).map(Capability::new);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public Set<DataIdentifier> getLink(DataIdentifier identifier, boolean isAmend) {
        String sql = "SELECT target FROM LINK WHERE source = ? AND amend = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier.toByteArray());
            stmt.setBoolean(2, isAmend);
            try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
                Set<DataIdentifier> result = new HashSet<>();
                while (rs.next())
                    result.add(new DataIdentifier(rs.getBytes("identifier")));
                return result;
            }
        }catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }

    }

    @Override
    public Optional<Boolean> makePermanentIfExists(DataIdentifier identifier){
        try {
            Optional<Data> data = getData(identifier);
            if(data.isPresent()) {
                if(data.get().isTemporary()) {
                    addToLog(identifier);
                    makePermanent(identifier);
                    return Optional.of(true);
                } else {
                    return Optional.of(false);
                }
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }



    @Override
    public Set<DataIdentifier> getAllIdentifiers() {
        String sql = "SELECT identifier FROM DATA";
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
    public Set<DataIdentifier> getNewIdentifiers(){
        String selctSql = "SELECT identifier FROM FINALIZATION_LOG ";
        try (ResultSet rsSelect = conn.createStatement().executeQuery(selctSql)) {
            Set<DataIdentifier> result = new HashSet<>();
            while (rsSelect.next()) result.add(new DataIdentifier(rsSelect.getBytes("identifier")));
            String deleteSql = "DELETE FROM FINALIZATION_LOG";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.executeUpdate();
            }
            return result;
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public long getPointer() {
        try {
            return Long.parseLong(getVariable("pointer"));
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    @Override
    public void setPointer(long pointer) {
        try {
            setVariable("pointer", ""+pointer);
        } catch (SQLException e) {
            throw new MisconfigurationException(e.getMessage());
        }
    }

    private Optional<Data> getData(DataIdentifier identifier) throws SQLException {
        String sql = "SELECT * FROM DATA WHERE identifier = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier.toByteArray());
            try (ResultSet rs = stmt.executeQuery()) {
                return Data.buildFrom(rs);
            }
        }
    }

    private void setData(Data data) throws SQLException {
        String sql = "INSERT INTO DATA(identifier, capability, temporary) VALUES(?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, data.getIdentifier());
            stmt.setBytes(2, data.getCapability());
            stmt.setBoolean(3, data.isTemporary());
            stmt.executeUpdate();
        }
    }

    private void setLink(Link link) throws SQLException {
        String sql = "INSERT INTO LINK(source, target, amend) VALUES(?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, link.getSource());
            stmt.setBytes(2, link.getTarget());
            stmt.setBoolean(3, link.isAmend());
            stmt.executeUpdate();
        }
    }

    private boolean hasLink(Link link) throws SQLException {
        String sql = "SELECT  * FROM LINK LIMIT 1 WHERE source = ? AND target = ? AND amend = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, link.getSource());
            stmt.setBytes(2, link.getTarget());
            stmt.setBoolean(3, link.isAmend());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void addToLog(DataIdentifier identifier) throws SQLException {
        String sql = "INSERT INTO FINALIZATION_LOG(identifier) VALUES(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier.toByteArray());
            stmt.executeUpdate();
        }
    }

    private void makePermanent(DataIdentifier identifier) throws SQLException {
        String sql = "UPDATE DATA SET temporary = false WHERE identifier = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, identifier.toByteArray());
            stmt.executeUpdate();
        }
    }

    private String getVariable(String variable) throws SQLException {
        String sql = "SELECT value FROM VARIABLES WHERE key = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, variable);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getString("value");
            }
        }
    }

    private void setVariable(String variable, String value) throws SQLException {
        String sql = "UPDATE VARIABLES SET value = ? WHERE key = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, variable);
            stmt.executeUpdate();
        }
    }

    private Connection connectOrCreate() {
        Connection con;
        try {
            // only allow to open existing databases
            con = connect(";IFEXISTS=TRUE");
        } catch (SQLException e1) {
            try {
                // create an empty cache with a pointer to null
                con = createCache(fallbackBlock);
            } catch (SQLException e2) {
                throw new MisconfigurationException(e2.getMessage());
            }
        }
        return con;
    }

    private Connection connect(String params) throws SQLException {
        String db = account.getIdentifier();
        // combination of file password (used for .db file encryption) and user password
        String pwd = String.format("%s-%s %<s", db, account.getPassword());

        // using h2 in auto mixed mode
        String url = String.format("jdbc:h2:./%s/%s;CIPHER=%s;AUTO_SERVER=TRUE%s", outputDirectory, db, CIPHER, params);
        return DriverManager.getConnection(url, db, pwd);
    }

    private Connection createCache(long pointer) throws SQLException {
        // only allow the creation of non-existent databases
        Connection conn = connect(";IFEXISTS=FALSE");

        // create schema
        RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream("/schema.sql")));

        // initialize pointers to the blocks of the connected blockchain
        String sql = "INSERT INTO VARIABLES(key, value) VALUES(?, ?), (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "pointer");
            stmt.setLong(2, pointer);
            stmt.setString(3, "poll-pointer");
            stmt.setLong(4, pointer);
            stmt.executeUpdate();
        }

        return conn;
    }
}
