package net.exylia.commons.database.core;

/**
 * Interface for database tables, providing the contract for any table
 * that needs to be created and managed in the database.
 */
public interface DatabaseTable {

    /**
     * Creates the table in the database if it doesn't exist.
     * This method should handle the SQL statement to create the table structure.
     */
    void createTable();

    /**
     * Returns the name of the table.
     *
     * @return The table name as a string
     */
    String getTableName();
}
