package net.exylia.commons.database.enums;

/**
 * Enumeration of supported database types.
 */
public enum DatabaseType {
    SQLITE,
    MYSQL,
    MARIADB;

    /**
     * Returns the JDBC driver class name for this database type.
     *
     * @return The JDBC driver class name.
     */
    public String getDriverClassName() {
        return switch (this) {
            case SQLITE -> "org.sqlite.JDBC";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case MARIADB -> "org.mariadb.jdbc.Driver";
        };
    }

    /**
     * Returns the JDBC URL prefix for this database type.
     *
     * @return The JDBC URL prefix.
     */
    public String getJdbcPrefix() {
        return switch (this) {
            case SQLITE -> "jdbc:sqlite:";
            case MYSQL -> "jdbc:mysql://";
            case MARIADB -> "jdbc:mariadb://";
        };
    }
}