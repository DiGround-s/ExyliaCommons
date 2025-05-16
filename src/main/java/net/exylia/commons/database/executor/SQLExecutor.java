package net.exylia.commons.database.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes SQL statements with parameter binding.
 */
public class SQLExecutor implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SQLExecutor.class.getName());

    private final Connection connection;
    private final boolean manageConnection;

    /**
     * Creates a new SQL executor with the given connection.
     * The connection will not be closed when the executor is closed.
     *
     * @param connection The database connection
     */
    public SQLExecutor(Connection connection) {
        this(connection, false);
    }

    /**
     * Creates a new SQL executor with the given connection.
     *
     * @param connection The database connection
     * @param manageConnection Whether the executor should close the connection when it is closed
     */
    public SQLExecutor(Connection connection, boolean manageConnection) {
        this.connection = connection;
        this.manageConnection = manageConnection;
    }

    /**
     * Executes an update statement.
     *
     * @param sql The SQL statement
     * @param params The parameters to bind
     * @return The number of affected rows
     * @throws SQLException If an error occurs
     */
    public int update(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setParams(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error executing update: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Executes a query statement.
     *
     * @param sql The SQL statement
     * @param params The parameters to bind
     * @return The result set
     * @throws SQLException If an error occurs
     */
    public ResultSet query(String sql, Object... params) throws SQLException {
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            setParams(stmt, params);
            return stmt.executeQuery();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error executing query: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Executes a query statement and returns a single value from the first row.
     *
     * @param sql The SQL statement
     * @param columnIndex The column index to retrieve (1-based)
     * @param params The parameters to bind
     * @param <T> The expected return type
     * @return The value or null if no rows found
     * @throws SQLException If an error occurs
     */
    @SuppressWarnings("unchecked")
    public <T> T queryScalar(String sql, int columnIndex, Object... params) throws SQLException {
        try (ResultSet rs = query(sql, params)) {
            if (rs.next()) {
                return (T) rs.getObject(columnIndex);
            }
            return null;
        }
    }

    /**
     * Executes a query statement and returns a single value from the first row.
     *
     * @param sql The SQL statement
     * @param columnName The column name to retrieve
     * @param params The parameters to bind
     * @param <T> The expected return type
     * @return The value or null if no rows found
     * @throws SQLException If an error occurs
     */
    @SuppressWarnings("unchecked")
    public <T> T queryScalar(String sql, String columnName, Object... params) throws SQLException {
        try (ResultSet rs = query(sql, params)) {
            if (rs.next()) {
                return (T) rs.getObject(columnName);
            }
            return null;
        }
    }

    /**
     * Executes a query statement and returns whether any rows were found.
     *
     * @param sql The SQL statement
     * @param params The parameters to bind
     * @return True if at least one row was found
     * @throws SQLException If an error occurs
     */
    public boolean exists(String sql, Object... params) throws SQLException {
        try (ResultSet rs = query(sql, params)) {
            return rs.next();
        }
    }

    /**
     * Begins a transaction.
     *
     * @throws SQLException If an error occurs
     */
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    /**
     * Commits the current transaction.
     *
     * @throws SQLException If an error occurs
     */
    public void commit() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Rolls back the current transaction.
     *
     * @throws SQLException If an error occurs
     */
    public void rollback() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }

    /**
     * Gets the underlying connection.
     *
     * @return The connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Sets the parameters for a prepared statement.
     *
     * @param stmt The prepared statement
     * @param params The parameters to bind
     * @throws SQLException If an error occurs
     */
    private void setParams(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * Closes the executor and optionally the connection.
     *
     * @throws SQLException If an error occurs
     */
    @Override
    public void close() throws SQLException {
        if (manageConnection && connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
