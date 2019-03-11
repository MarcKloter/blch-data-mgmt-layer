package bdml.blockchain.cache;


import bdml.services.BlockTime;
import bdml.services.QueryResult;
import bdml.services.exceptions.MisconfigurationException;
import bdml.services.exceptions.MissingConfigurationException;

import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.h2.tools.RunScript;


public class CacheImpl implements Cache {
    // mandatory configuration properties
    private static final String OUTPUT_DIRECTORY_KEY = "bdml.output.directory";
    private static final String DB_NAME_KEY = "bdml.output.blockchain.cache";

    private final String outputDirectory;
    private final String dbName;
    private final Connection conn;

    public CacheImpl(Properties configuration) {
        // load configuration
        this.outputDirectory = getProperty(configuration, OUTPUT_DIRECTORY_KEY);
        this.dbName = getProperty(configuration, DB_NAME_KEY);
        this.conn = connectOrCreate();
    }

    @Override
    public void finalizeBlock(long no) {
        try {
            String sql = "UPDATE BLOCK SET next = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, no+1);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public long nextBlock() {
        try {
            String sql = "SELECT * FROM BLOCK";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if(rs.next()) {
                        return rs.getLong("next");
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void addDocumentIndex(byte[] identifier, TxtIndex txtIndex) {
        try {
            String sql = "INSERT INTO DATA(identifier, block, index, hash, plain) VALUES(?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setLong(2, txtIndex.blockNo);
                stmt.setLong(3, txtIndex.txtIndex);
                stmt.setBytes(4, txtIndex.hash);
                stmt.setBoolean(5, txtIndex.isPlain);

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void addAccessIndex(byte[] identifier, long blockNo, byte[] hash) {
        try {
            String sql = "INSERT INTO ACCESS(identifier, block, hash) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setLong(2, blockNo);
                stmt.setBytes(3, hash);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void addAmendIndex(byte[] identifier, long blockNo, byte[] hash) {
        try {
            String sql = "INSERT INTO AMEND(identifier, block, hash) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setLong(2, blockNo);
                stmt.setBytes(3, hash);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private TxtIndex extractTxtIndex(ResultSet rs) throws SQLException{
        long blockNo = rs.getLong("block");
        long index = rs.getLong("index");
        byte[] hash = rs.getBytes("hash");
        boolean isPlain = rs.getBoolean("plain");
        return new TxtIndex(blockNo,index, hash, isPlain);
    }

    @Override
    public List<TxtIndex> getIndex(byte[] identifier) {
        try {
            String sql = "SELECT * FROM DATA WHERE identifier = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1,identifier);
                List<TxtIndex> res = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        res.add(extractTxtIndex(rs));
                    }
                    return res;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public List<byte[]> getAllFinalizedAccessTokens(long blockNoStart , long blockNoEnd, byte[] identifier) {
        try (Connection conn = connectOrCreate()) {
            String sql = "SELECT hash FROM ACCESS WHERE block >= ? AND  block < ? AND identifier = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1,blockNoStart);
                stmt.setLong(2,blockNoEnd);
                stmt.setBytes(3,identifier);

                try (ResultSet rs = stmt.executeQuery()) {
                    List<byte[]> values = new ArrayList<>();
                    while (rs.next()) {
                        values.add( rs.getBytes("hash"));
                    }
                    return values;
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public List<byte[]> getAllAmendments(long blockNoStart ,long blockNoEnd) {
        try (Connection conn = connectOrCreate()) {
            String sql = "SELECT hash FROM AMEND WHERE block >= ? AND  block < ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1,blockNoStart);
                stmt.setLong(2,blockNoEnd);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<byte[]> values = new ArrayList<>();
                    while (rs.next()) {
                        values.add( rs.getBytes("hash"));
                    }
                    return values;
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public List<byte[]> getAllAmendmentFor(byte[] identifier) {
        try (Connection conn = connectOrCreate()) {
            String sql = "SELECT hash FROM AMEND WHERE  identifier = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1,identifier);

                try (ResultSet rs = stmt.executeQuery()) {
                    List<byte[]> values = new ArrayList<>();
                    while (rs.next()) {
                        values.add( rs.getBytes("hash"));
                    }
                    return values;
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public List<QueryResult<byte[]>> getAllPlain(long blockNoStart ,long blockNoEnd) {
        try (Connection conn = connectOrCreate()) {
            String sql = "SELECT identifier, block, index FROM DATA WHERE block >= ? AND  block < ? AND plain = true";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1,blockNoStart);
                stmt.setLong(2,blockNoEnd);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<QueryResult<byte[]>> values = new ArrayList<>();
                    while (rs.next()) {
                        values.add( new QueryResult<>(
                                rs.getBytes("identifier"),
                                new BlockTime(rs.getLong("block"),rs.getLong("index")),
                                true
                        ));
                    }
                    return values;
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String getProperty(Properties configuration, String property) {
        if (!configuration.containsKey(property))
            throw new MissingConfigurationException(property);

        return configuration.getProperty(property);
    }


    private Connection connectOrCreate() {
        try {
            // only allow to open existing databases
            return connect(";IFEXISTS=TRUE");
        } catch (SQLException e1) {
            try {
                // create an empty cache with a pointer to null
                return createCache();
            } catch (SQLException e2) {
                throw new MisconfigurationException(e2.getMessage());
            }
        }
    }

    private Connection connect(String params) throws SQLException {
        // using h2 in auto mixed mode
        String url = String.format("jdbc:h2:./%s/%s;AUTO_SERVER=TRUE%s", outputDirectory, dbName, params);
        return DriverManager.getConnection(url, null, null);
    }

    private Connection createCache() throws SQLException {
        // only allow the creation of non-existent databases
        Connection conn = connect(";IFEXISTS=FALSE");

        // create schema
        RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream("/cache_schema.sql")));

        //ad a zero as initial value
        String sql = "INSERT INTO BLOCK(next) VALUES(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, 0);
            stmt.executeUpdate();
        }


        return conn;
    }

    @Override
    public void addPendingFrame(byte[] identifier, byte[] frame, boolean encrypted) {
        try {
            String sql = "INSERT INTO PENDING_DATA(identifier, frame, plain) VALUES(?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setBytes(2, frame);
                stmt.setBoolean(3, !encrypted);

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void removePendingFrame(byte[] identifier) {
        try {
            String sql = "DELETE FROM PENDING_DATA WHERE identifier = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void addPendingToken(byte[] identifier, byte[] token) {
        try {
            String sql = "INSERT INTO PENDING_TOKEN(identifier, token) VALUES(?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setBytes(2, token);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void addPendingAmend(byte[] identifier, byte[] token) {
        try {
            String sql = "INSERT INTO PENDING_AMENDMENT(identifier, token) VALUES(?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setBytes(2, token);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void removePendingToken(byte[] identifier, byte[] token) {
        try {
            String sql = "DELETE FROM PENDING_TOKEN WHERE identifier = ? AND token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setBytes(2, token);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void removePendingAmend(byte[] identifier, byte[] token) {
        try {
            String sql = "DELETE FROM PENDING_AMENDMENT WHERE identifier = ? AND token = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                stmt.setBytes(2, token);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public QueryResult<byte[]> getPendingDocument(byte[] identifier) {
        try {
            String sql = "SELECT frame, plain FROM PENDING_DATA WHERE identifier = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBytes(1, identifier);
                try (ResultSet rs = stmt.executeQuery()) {
                    if(rs.next()) {
                        return new QueryResult<>(
                                rs.getBytes("frame"),
                                null, rs.getBoolean("plain")
                        );
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
