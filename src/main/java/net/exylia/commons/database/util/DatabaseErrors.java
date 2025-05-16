package net.exylia.commons.database.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging database errors and messages.
 */
public class DatabaseErrors {
    private static final Logger LOGGER = Logger.getLogger(DatabaseErrors.class.getName());

    private DatabaseErrors() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Logs a database error with a formatted message.
     *
     * @param operation The database operation that failed
     * @param table The table being operated on
     * @param error The error that occurred
     */
    public static void logDatabaseError(String operation, String table, Throwable error) {
        LOGGER.log(Level.SEVERE, String.format("Database error during %s on table '%s': %s",
                operation, table, error.getMessage()), error);
    }

    /**
     * Logs a database error with a formatted message.
     *
     * @param operation The database operation that failed
     * @param error The error that occurred
     */
    public static void logDatabaseError(String operation, Throwable error) {
        LOGGER.log(Level.SEVERE, String.format("Database error during %s: %s",
                operation, error.getMessage()), error);
    }

    /**
     * Logs a table creation error.
     *
     * @param table The table that failed to create
     * @param error The error that occurred
     */
    public static void logTableCreationError(String table, Throwable error) {
        logDatabaseError("table creation", table, error);
    }

    /**
     * Logs a data insertion error.
     *
     * @param table The table being inserted into
     * @param error The error that occurred
     */
    public static void logInsertError(String table, Throwable error) {
        logDatabaseError("insert", table, error);
    }

    /**
     * Logs a data update error.
     *
     * @param table The table being updated
     * @param error The error that occurred
     */
    public static void logUpdateError(String table, Throwable error) {
        logDatabaseError("update", table, error);
    }

    /**
     * Logs a data deletion error.
     *
     * @param table The table being deleted from
     * @param error The error that occurred
     */
    public static void logDeleteError(String table, Throwable error) {
        logDatabaseError("delete", table, error);
    }

    /**
     * Logs a data query error.
     *
     * @param table The table being queried
     * @param error The error that occurred
     */
    public static void logQueryError(String table, Throwable error) {
        logDatabaseError("query", table, error);
    }

    /**
     * Logs a transaction error.
     *
     * @param operation The transaction operation (begin, commit, rollback)
     * @param error The error that occurred
     */
    public static void logTransactionError(String operation, Throwable error) {
        logDatabaseError("transaction " + operation, error);
    }

    /**
     * Logs a connection error.
     *
     * @param error The error that occurred
     */
    public static void logConnectionError(Throwable error) {
        logDatabaseError("connection", error);
    }

    /**
     * Logs a serialization error.
     *
     * @param operation The serialization operation (serialize, deserialize)
     * @param error The error that occurred
     */
    public static void logSerializationError(String operation, Throwable error) {
        logDatabaseError(operation, error);
    }
}
