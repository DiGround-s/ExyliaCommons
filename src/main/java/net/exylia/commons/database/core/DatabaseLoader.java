package net.exylia.commons.database.core;

import net.exylia.commons.database.connection.ConnectionPool;
import net.exylia.commons.database.connection.DatabaseCredentials;
import net.exylia.commons.database.enums.DatabaseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for loading and managing database tables.
 * This class should be extended by plugins to create a database loader specific to their needs.
 */
public abstract class DatabaseLoader implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(DatabaseLoader.class.getName());

    private ConnectionPool connectionPool;
    private final List<DatabaseTable> tables = new ArrayList<>();
    private final Map<Class<? extends DatabaseTable>, DatabaseTable> tableMap = new HashMap<>();

    /**
     * Loads the database with the given credentials.
     *
     * @param credentials The database credentials
     */
    public void load(DatabaseCredentials credentials) {
        try {
            // Initialize the connection pool
            this.connectionPool = new ConnectionPool(credentials);

            // Register and create tables
            registerTables();
            createTables();

            LOGGER.info("Database loaded successfully with " + tables.size() + " tables.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load database: " + e.getMessage(), e);
        }
    }

    /**
     * Registers the database tables.
     * This method should be implemented by subclasses to register their tables.
     */
    protected abstract void registerTables();

    /**
     * Adds a table to the registry.
     *
     * @param table The table to add
     * @param <T> The type of the table
     * @return The added table
     */
    @SuppressWarnings("unchecked")
    protected <T extends DatabaseTable> T addTable(T table) {
        tables.add(table);
        tableMap.put((Class<? extends DatabaseTable>) table.getClass(), table);
        return table;
    }

    /**
     * Creates all registered tables.
     */
    private void createTables() {
        for (DatabaseTable table : tables) {
            try {
                table.createTable();
                LOGGER.info("Created table: " + table.getTableName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to create table " + table.getTableName() + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets a registered table by its class.
     *
     * @param tableClass The class of the table
     * @param <T> The type of the table
     * @return The table, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends DatabaseTable> T getTable(Class<T> tableClass) {
        return (T) tableMap.get(tableClass);
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
     * Gets the database type.
     *
     * @return The database type
     */
    public DatabaseType getDatabaseType() {
        return connectionPool != null ? connectionPool.getDatabaseType() : null;
    }

    /**
     * Gets all registered tables.
     *
     * @return A list of all tables
     */
    public List<DatabaseTable> getTables() {
        return new ArrayList<>(tables);
    }

    /**
     * Closes the database loader and its resources.
     */
    @Override
    public void close() {
        if (connectionPool != null) {
            connectionPool.close();
            LOGGER.info("Database connection closed.");
        }
    }
}
