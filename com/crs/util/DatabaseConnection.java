package com.crs.util;
 
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class DatabaseConnection {
    /* Connection pool config */
    private static final int POOL_SIZE = 10;
    private static final int CONNECTION_TIMEOUT = 5_000; 
    private static final int QUERY_TIMEOUT_SECS = 30;

    /* Profiles */
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String H2_URL = "jdbc:h2:mem:crsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String H2_USER = "sa";
    private static final String H2_PASS = "";
 
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/crs_db"
        + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    /* Stats */
    private final String driverClass;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final BlockingQueue<Connection> pool;
    private volatile boolean initialised = false;
    private volatile boolean simulationMode = false;

    private static final LoggerUtil LOG = LoggerUtil.getInstance();

    /* Singleton instance */
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    public  static DatabaseConnection getInstance() { return INSTANCE; }
 
    private DatabaseConnection() {
        Properties cfg = loadProperties();
 
        String profile = System.getProperty("db.profile",
            cfg.getProperty("db.profile", "h2")).toLowerCase();
 
        if ("mysql".equals(profile)) {
            driverClass = cfg.getProperty("db.driver", MYSQL_DRIVER);
            jdbcUrl     = cfg.getProperty("db.url", MYSQL_URL);
            username    = cfg.getProperty("db.username", System.getenv().getOrDefault("DB_USER", "crs_user"));
            password    = cfg.getProperty("db.password", System.getenv().getOrDefault("DB_PASS", "crs_password"));
        } else {
            driverClass = cfg.getProperty("db.driver", H2_DRIVER);
            jdbcUrl     = cfg.getProperty("db.url", H2_URL);
            username    = cfg.getProperty("db.username", H2_USER);
            password    = cfg.getProperty("db.password", H2_PASS);
        }
 
        pool = new ArrayBlockingQueue<>(POOL_SIZE);
        initialise();
    }

    /* Public API */
    public Connection getConnection() throws SQLException {
        if (simulationMode) {
            LOG.logWarning("DatabaseConnection.getConnection — SIMULATION MODE: returning null.");
            return null;
        }
 
        try {
            Connection conn = pool.poll();
            if (conn == null || conn.isClosed()) {
                conn = createConnection();
            }
            return wrapForPool(conn);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a connection.", e);
        }
    }

    public void closeAll() {
        pool.forEach(conn -> {
            try { conn.close(); } catch (SQLException ignored) {}
        });
        pool.clear();
        LOG.logInfo("DatabaseConnection.closeAll — connection pool closed.");
    }

    public boolean isConnected() {
        if (simulationMode) return false;
        try (Connection test = getConnection()) {
            return test != null && !test.isClosed() && test.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public String healthCheck() {
        if (simulationMode) return "SIMULATION (no real DB connected)";
        try (Connection conn = getConnection();
             Statement  stmt = conn.createStatement()) {
            stmt.setQueryTimeout(2);
            stmt.execute("SELECT 1");
            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String getJdbcUrl() { return jdbcUrl; }

    public String getDriverClass() { return driverClass; }

    public int getPoolSize() { return pool.size(); }

    /* Private helpers */
    private void initialise() {
        try {
            Class.forName(driverClass);
            for (int i = 0; i < POOL_SIZE; i++) {
                pool.offer(createConnection());
            }
            initialised = true;
            LOG.logInfo("DatabaseConnection.initialise — pool ready: "
                + POOL_SIZE + " connections, url=" + jdbcUrl);
        } catch (ClassNotFoundException e) {
            simulationMode = true;
            LOG.logWarning("DatabaseConnection.initialise — driver not found ("
                + driverClass + "). Running in SIMULATION MODE.");
        } catch (SQLException e) {
            simulationMode = true;
            LOG.logWarning("DatabaseConnection.initialise — cannot connect to "
                + jdbcUrl + ": " + e.getMessage() + ". Running in SIMULATION MODE.");
        }
    }
 
    private Connection createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user",     username);
        props.setProperty("password", password);
        props.setProperty("connectTimeout", String.valueOf(CONNECTION_TIMEOUT));
        return DriverManager.getConnection(jdbcUrl, props);
    }

    private Connection wrapForPool(Connection real) {
        return new PooledConnection(real, pool);
    }
 
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is != null) props.load(is);
        } catch (IOException ignored) {}
        return props;
    }

    /* PooledConnection wrapper */
    private static final class PooledConnection implements Connection {
 
        private final Connection delegate;
        private final BlockingQueue<Connection> pool;
        private boolean closed = false;
 
        PooledConnection(Connection delegate, BlockingQueue<Connection> pool) {
            this.delegate = delegate;
            this.pool = pool;
        }
 
        @Override
        public void close() throws SQLException {
            if (!closed) {if (!pool.offer(delegate)) {
                    delegate.close();
                }
                closed = true;
            }
        }
 
        @Override public boolean isClosed() throws SQLException {
            return closed || delegate.isClosed();
        }

        @Override public Statement createStatement() throws SQLException { return delegate.createStatement(); }
        @Override public PreparedStatement prepareStatement(String sql) throws SQLException { return delegate.prepareStatement(sql); }
        @Override public CallableStatement prepareCall(String sql) throws SQLException { return delegate.prepareCall(sql); }
        @Override public String nativeSQL(String sql) throws SQLException { return delegate.nativeSQL(sql); }
        @Override public void setAutoCommit(boolean b) throws SQLException { delegate.setAutoCommit(b); }
        @Override public boolean getAutoCommit() throws SQLException { return delegate.getAutoCommit(); }
        @Override public void commit() throws SQLException { delegate.commit(); }
        @Override public void rollback() throws SQLException { delegate.rollback(); }
        @Override public DatabaseMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
        @Override public void setReadOnly(boolean b) throws SQLException { delegate.setReadOnly(b); }
        @Override public boolean isReadOnly() throws SQLException { return delegate.isReadOnly(); }
        @Override public void setCatalog(String catalog) throws SQLException { delegate.setCatalog(catalog); }
        @Override public String getCatalog() throws SQLException { return delegate.getCatalog(); }
        @Override public void setTransactionIsolation(int l) throws SQLException { delegate.setTransactionIsolation(l); }
        @Override public int getTransactionIsolation() throws SQLException { return delegate.getTransactionIsolation(); }
        @Override public SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
        @Override public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
        @Override public Statement createStatement(int t, int c) throws SQLException { return delegate.createStatement(t, c); }
        @Override public PreparedStatement prepareStatement(String s, int t, int c) throws SQLException { return delegate.prepareStatement(s, t, c); }
        @Override public CallableStatement prepareCall(String s, int t, int c) throws SQLException { return delegate.prepareCall(s, t, c); }
        @Override public java.util.Map<String, Class<?>> getTypeMap() throws SQLException { return delegate.getTypeMap(); }
        @Override public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException { delegate.setTypeMap(map); }
        @Override public void setHoldability(int h) throws SQLException { delegate.setHoldability(h); }
        @Override public int getHoldability() throws SQLException { return delegate.getHoldability(); }
        @Override public Savepoint setSavepoint() throws SQLException { return delegate.setSavepoint(); }
        @Override public Savepoint setSavepoint(String name) throws SQLException { return delegate.setSavepoint(name); }
        @Override public void rollback(Savepoint sp) throws SQLException { delegate.rollback(sp); }
        @Override public void releaseSavepoint(Savepoint sp) throws SQLException { delegate.releaseSavepoint(sp); }
        @Override public Statement createStatement(int t, int c, int h) throws SQLException { return delegate.createStatement(t, c, h); }
        @Override public PreparedStatement prepareStatement(String s, int t, int c, int h) throws SQLException { return delegate.prepareStatement(s, t, c, h); }
        @Override public CallableStatement prepareCall(String s, int t, int c, int h) throws SQLException { return delegate.prepareCall(s, t, c, h); }
        @Override public PreparedStatement prepareStatement(String s, int[] ci) throws SQLException { return delegate.prepareStatement(s, ci); }
        @Override public PreparedStatement prepareStatement(String s, String[] cn) throws SQLException { return delegate.prepareStatement(s, cn); }
        @Override public PreparedStatement prepareStatement(String s, int ag) throws SQLException { return delegate.prepareStatement(s, ag); }
        @Override public java.sql.Clob createClob() throws SQLException { return delegate.createClob(); }
        @Override public java.sql.Blob createBlob() throws SQLException { return delegate.createBlob(); }
        @Override public java.sql.NClob createNClob() throws SQLException { return delegate.createNClob(); }
        @Override public java.sql.SQLXML createSQLXML() throws SQLException { return delegate.createSQLXML(); }
        @Override public boolean isValid(int timeout) throws SQLException { return delegate.isValid(timeout); }
        @Override public void setClientInfo(String name, String value) throws java.sql.SQLClientInfoException { try { delegate.setClientInfo(name, value); } catch (java.sql.SQLClientInfoException e) { throw e; } }
        @Override public void setClientInfo(Properties props) throws java.sql.SQLClientInfoException { try { delegate.setClientInfo(props); } catch (java.sql.SQLClientInfoException e) { throw e; } }
        @Override public String getClientInfo(String name) throws SQLException { return delegate.getClientInfo(name); }
        @Override public Properties getClientInfo() throws SQLException { return delegate.getClientInfo(); }
        @Override public java.sql.Array createArrayOf(String t, Object[] e) throws SQLException { return delegate.createArrayOf(t, e); }
        @Override public java.sql.Struct createStruct(String t, Object[] a) throws SQLException { return delegate.createStruct(t, a); }
        @Override public void setSchema(String s) throws SQLException { delegate.setSchema(s); }
        @Override public String getSchema() throws SQLException { return delegate.getSchema(); }
        @Override public void abort(java.util.concurrent.Executor ex) throws SQLException { delegate.abort(ex); }
        @Override public void setNetworkTimeout(java.util.concurrent.Executor ex, int ms) throws SQLException { delegate.setNetworkTimeout(ex, ms); }
        @Override public int getNetworkTimeout() throws SQLException { return delegate.getNetworkTimeout(); }
        @Override public <T> T unwrap(Class<T> i) throws SQLException { return delegate.unwrap(i); }
        @Override public boolean isWrapperFor(Class<?> i) throws SQLException { return delegate.isWrapperFor(i); }
    }
}