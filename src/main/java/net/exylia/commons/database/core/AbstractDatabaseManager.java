package net.exylia.commons.database.core;

import net.exylia.commons.database.connection.ConnectionPool;
import net.exylia.commons.database.connection.DatabaseCredentials;
import net.exylia.commons.database.executor.SQLExecutor;
import net.exylia.commons.database.util.DatabaseErrors;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Abstract database manager that handles connections and transactions.
 * This class provides a foundation for database operations with built-in error handling.
 */
public abstract class AbstractDatabaseManager implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(AbstractDatabaseManager.class.getName());

    private final ConnectionPool connectionPool;

    /**
     * Creates a new database manager with the given credentials.
     *
     * @param credentials The database credentials
     */
    public AbstractDatabaseManager(DatabaseCredentials credentials) {
        this.connectionPool = new ConnectionPool(credentials);
    }

    /**
     * Creates a new database manager with the given connection pool.
     *
     * @param connectionPool The connection pool
     */
    public AbstractDatabaseManager(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * Gets a connection from the pool.
     *
     * @return A database connection
     * @throws SQLException If a connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            DatabaseErrors.logConnectionError(e);
            throw e;
        }
    }

    /**
     * Creates a new SQL executor with a new connection.
     * The connection will be closed when the executor is closed.
     *
     * @return A new SQL executor
     * @throws SQLException If a connection cannot be obtained
     */
    public SQLExecutor createExecutor() throws SQLException {
        return new SQLExecutor(getConnection(), true);
    }

    /**
     * Executes a database operation with automatic resource management.
     *
     * @param operation The operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     * @throws SQLException If an error occurs
     */
    public <T> T executeOperation(DatabaseOperation<T> operation) throws SQLException {
        try (Connection conn = getConnection();
             SQLExecutor executor = new SQLExecutor(conn)) {
            return operation.execute(executor);
        } catch (SQLException e) {
            DatabaseErrors.logDatabaseError("executeOperation", e);
            throw e;
        }
    }

    /**
     * Executes a database operation within a transaction with automatic resource management.
     *
     * @param operation The operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     * @throws SQLException If an error occurs
     */
    public <T> T executeTransaction(DatabaseOperation<T> operation) throws SQLException {
        Connection conn = null;
        SQLExecutor executor = null;

        try {
            conn = getConnection();
            executor = new SQLExecutor(conn);

            // Begin transaction
            conn.setAutoCommit(false);

            // Execute the operation
            T result = operation.execute(executor);

            // Commit transaction
            conn.commit();

            return result;
        } catch (SQLException e) {
            // Rollback transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    DatabaseErrors.logTransactionError("rollback", rollbackEx);
                }
            }

            DatabaseErrors.logTransactionError("execute", e);
            throw e;
        } finally {
            // Restore auto-commit
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    DatabaseErrors.logTransactionError("restore autoCommit", autoCommitEx);
                }
            }

            // Close resources
            if (executor != null) {
                try {
                    executor.close();
                } catch (SQLException closeEx) {
                    DatabaseErrors.logDatabaseError("close executor", closeEx);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    DatabaseErrors.logDatabaseError("close connection", closeEx);
                }
            }
        }
    }

    /**
     * Gets the connection pool.
     *
     * @return The connection pool
     */
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    /**
     * Closes the database manager and its resources.
     */
    @Override
    public void close() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

    /**
     * Functional interface for database operations.
     *
     * @param <T> The return type of the operation
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        /**
         * Executes the database operation.
         *
         * @param executor The SQL executor
         * @return The result of the operation
         * @throws SQLException If an error occurs
         */
        T execute(SQLExecutor executor) throws SQLException;
    }
}
