package net.exylia.commons.database.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.exylia.commons.database.enums.DatabaseType;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connections using HikariCP connection pool.
 */
public class ConnectionPool implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class.getName());

    private final HikariDataSource dataSource;
    private final DatabaseCredentials credentials;

    /**
     * Creates a new connection pool with the given credentials.
     *
     * @param credentials The database credentials
     */
    public ConnectionPool(DatabaseCredentials credentials) {
        this.credentials = credentials;

        // Setup HikariCP configuration
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(credentials.getJdbcUrl());
        config.setDriverClassName(credentials.getType().getDriverClassName());

        // Configure SQLite database file if needed
        if (credentials.getType() == DatabaseType.SQLITE) {
            ensureSQLiteFileExists(credentials.getSqliteFile());
        } else {
            // MySQL/MariaDB authentication
            config.setUsername(credentials.getUsername());
            config.setPassword(credentials.getPassword());
        }

        // Pool configuration
        config.setMaximumPoolSize(credentials.getMaxPoolSize());
        config.setMinimumIdle(credentials.getMinIdle());
        config.setIdleTimeout(credentials.getIdleTimeout());
        config.setConnectionTimeout(credentials.getConnectionTimeout());
        config.setLeakDetectionThreshold(15000);
        config.setPoolName(credentials.getPoolName());

        // SQLite specific configuration
        if (credentials.getType() == DatabaseType.SQLITE) {
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "NORMAL");
            config.addDataSourceProperty("foreign_keys", "ON");
        }

        // MySQL/MariaDB specific configuration
        if (credentials.getType() == DatabaseType.MYSQL || credentials.getType() == DatabaseType.MARIADB) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
        }

        try {
            this.dataSource = new HikariDataSource(config);
            LOGGER.info("Successfully initialized connection pool for " + credentials.getType() + " database.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize connection pool: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }

    /**
     * Ensures that the SQLite database file exists.
     *
     * @param filePath The path to the SQLite database file
     */
    private void ensureSQLiteFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs()) {
                        LOGGER.warning("Failed to create parent directories for SQLite file: " + filePath);
                    }
                }

                if (!file.createNewFile()) {
                    LOGGER.warning("Failed to create new SQLite file: " + filePath);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to create SQLite database file: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets a connection from the pool.
     *
     * @return A database connection
     * @throws SQLException If a connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get database connection: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets the database credentials.
     *
     * @return The database credentials
     */
    public DatabaseCredentials getCredentials() {
        return credentials;
    }

    /**
     * Gets the database type.
     *
     * @return The database type
     */
    public DatabaseType getDatabaseType() {
        return credentials.getType();
    }

    /**
     * Closes the connection pool.
     */
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Connection pool closed.");
        }
    }
}
