package net.exylia.commons.database.connection;

import net.exylia.commons.database.enums.DatabaseType;

/**
 * Holds credentials and connection information for a database.
 */
public class DatabaseCredentials {
    private final DatabaseType type;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String sqliteFile;
    private final int maxPoolSize;
    private final int minIdle;
    private final long idleTimeout;
    private final long connectionTimeout;
    private final String poolName;

    /**
     * Builder pattern implementation for DatabaseCredentials
     */
    public static class Builder {
        // Required parameters
        private final DatabaseType type;

        // Optional parameters with default values
        private String host = "localhost";
        private int port = 3306;
        private String database = "database";
        private String username = "root";
        private String password = "";
        private String sqliteFile = "database.db";
        private int maxPoolSize = 10;
        private int minIdle = 2;
        private long idleTimeout = 60000;
        private long connectionTimeout = 30000;
        private String poolName = "ExyliaCommons-HikariPool";

        /**
         * Constructor with the required parameter
         *
         * @param type The database type
         */
        public Builder(DatabaseType type) {
            this.type = type;
        }

        /**
         * Sets the host for MySQL/MariaDB connection
         *
         * @param host The hostname
         * @return The builder instance
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the port for MySQL/MariaDB connection
         *
         * @param port The port number
         * @return The builder instance
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the database name
         *
         * @param database The database name
         * @return The builder instance
         */
        public Builder database(String database) {
            this.database = database;
            return this;
        }

        /**
         * Sets the username for authentication
         *
         * @param username The username
         * @return The builder instance
         */
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        /**
         * Sets the password for authentication
         *
         * @param password The password
         * @return The builder instance
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the SQLite file path
         *
         * @param sqliteFile The file path
         * @return The builder instance
         */
        public Builder sqliteFile(String sqliteFile) {
            this.sqliteFile = sqliteFile;
            return this;
        }

        /**
         * Sets the maximum connection pool size
         *
         * @param maxPoolSize The maximum number of connections
         * @return The builder instance
         */
        public Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        /**
         * Sets the minimum number of idle connections
         *
         * @param minIdle The minimum number of idle connections
         * @return The builder instance
         */
        public Builder minIdle(int minIdle) {
            this.minIdle = minIdle;
            return this;
        }

        /**
         * Sets the idle timeout in milliseconds
         *
         * @param idleTimeout The idle timeout
         * @return The builder instance
         */
        public Builder idleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        /**
         * Sets the connection timeout in milliseconds
         *
         * @param connectionTimeout The connection timeout
         * @return The builder instance
         */
        public Builder connectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Sets the connection pool name
         *
         * @param poolName The pool name
         * @return The builder instance
         */
        public Builder poolName(String poolName) {
            this.poolName = poolName;
            return this;
        }

        /**
         * Builds the DatabaseCredentials object
         *
         * @return A new DatabaseCredentials instance
         */
        public DatabaseCredentials build() {
            return new DatabaseCredentials(this);
        }
    }

    /**
     * Private constructor that takes a Builder
     *
     * @param builder The builder with configuration
     */
    private DatabaseCredentials(Builder builder) {
        this.type = builder.type;
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.username = builder.username;
        this.password = builder.password;
        this.sqliteFile = builder.sqliteFile;
        this.maxPoolSize = builder.maxPoolSize;
        this.minIdle = builder.minIdle;
        this.idleTimeout = builder.idleTimeout;
        this.connectionTimeout = builder.connectionTimeout;
        this.poolName = builder.poolName;
    }

    /**
     * Gets the database type
     *
     * @return The database type
     */
    public DatabaseType getType() {
        return type;
    }

    /**
     * Gets the JDBC URL for the database connection
     *
     * @return The JDBC URL
     */
    public String getJdbcUrl() {
        return switch (type) {
            case MYSQL, MARIADB -> type.getJdbcPrefix() + host + ":" + port + "/" + database
                    + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
            case SQLITE -> type.getJdbcPrefix() + sqliteFile;
        };
    }

    /**
     * Gets the host name
     *
     * @return The host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port number
     *
     * @return The port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the database name
     *
     * @return The database name
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Gets the username
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the SQLite file path
     *
     * @return The SQLite file path
     */
    public String getSqliteFile() {
        return sqliteFile;
    }

    /**
     * Gets the maximum connection pool size
     *
     * @return The maximum pool size
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Gets the minimum number of idle connections
     *
     * @return The minimum idle connections
     */
    public int getMinIdle() {
        return minIdle;
    }

    /**
     * Gets the idle timeout in milliseconds
     *
     * @return The idle timeout
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Gets the connection timeout in milliseconds
     *
     * @return The connection timeout
     */
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Gets the connection pool name
     *
     * @return The pool name
     */
    public String getPoolName() {
        return poolName;
    }
}
