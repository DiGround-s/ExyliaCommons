package net.exylia.commons.database.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes batch SQL statements for improved performance when doing many operations.
 */
public class SQLBatchExecutor implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SQLBatchExecutor.class.getName());

    private final Connection connection;
    private final boolean manageConnection;
    private final String sql;
    private final PreparedStatement statement;
    private final List<Object[]> parameterBatch;
    private final int batchSize;

    /**
     * Creates a new batch executor with the given connection and SQL statement.
     *
     * @param connection The database connection
     * @param sql The SQL statement to execute in batch
     * @param batchSize The number of statements to batch before executing
     * @throws SQLException If an error occurs
     */
    public SQLBatchExecutor(Connection connection, String sql, int batchSize) throws SQLException {
        this(connection, sql, batchSize, false);
    }

    /**
     * Creates a new batch executor with the given connection and SQL statement.
     *
     * @param connection The database connection
     * @param sql The SQL statement to execute in batch
     * @param batchSize The number of statements to batch before executing
     * @param manageConnection Whether the executor should close the connection when it is closed
     * @throws SQLException If an error occurs
     */
    public SQLBatchExecutor(Connection connection, String sql, int batchSize, boolean manageConnection) throws SQLException {
        this.connection = connection;
        this.manageConnection = manageConnection;
        this.sql = sql;
        this.statement = connection.prepareStatement(sql);
        this.parameterBatch = new ArrayList<>();
        this.batchSize = batchSize;

        // Disable auto-commit for better performance
        boolean originalAutoCommit = connection.getAutoCommit();
        if (originalAutoCommit) {
            connection.setAutoCommit(false);
        }
    }

    /**
     * Adds a statement to the batch with the given parameters.
     *
     * @param params The parameters to bind
     * @throws SQLException If an error occurs
     */
    public void addBatch(Object... params) throws SQLException {
        parameterBatch.add(params);

        // Process the batch if it's full
        if (parameterBatch.size() >= batchSize) {
            executeBatch();
        }
    }

    /**
     * Executes the current batch of statements.
     *
     * @throws SQLException If an error occurs
     */
    public void executeBatch() throws SQLException {
        try {
            if (parameterBatch.isEmpty()) {
                return;
            }

            for (Object[] params : parameterBatch) {
                // Set parameters for this statement
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                statement.addBatch();
            }

            // Execute the batch
            statement.executeBatch();
            connection.commit();

            // Clear the batch for reuse
            parameterBatch.clear();

        } catch (SQLException e) {
            // Try to roll back on error
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE, "Error during rollback: " + rollbackEx.getMessage(), rollbackEx);
            }

            LOGGER.log(Level.SEVERE, "Error executing batch: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Gets the number of statements in the current batch.
     *
     * @return The batch size
     */
    public int getCurrentBatchSize() {
        return parameterBatch.size();
    }

    /**
     * Gets the maximum batch size before execution.
     *
     * @return The maximum batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Closes the executor, executing any remaining statements and restoring the connection state.
     *
     * @throws SQLException If an error occurs
     */
    @Override
    public void close() throws SQLException {
        try {
            // Execute any remaining statements
            if (!parameterBatch.isEmpty()) {
                executeBatch();
            }

            // Close the statement
            if (statement != null && !statement.isClosed()) {
                statement.close();
            }

            // Restore auto-commit
            connection.setAutoCommit(true);

            // Close the connection if we're managing it
            if (manageConnection && connection != null && !connection.isClosed()) {
                connection.close();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing batch executor: " + e.getMessage(), e);
            throw e;
        }
    }
}
