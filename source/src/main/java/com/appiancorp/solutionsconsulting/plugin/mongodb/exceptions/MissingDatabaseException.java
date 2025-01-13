package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

/**
 * Thrown to indicate that the specified MongoDB database does not exist.
 *
 * <p>This exception is typically used when an operation requires a
 * valid database name, but the provided database has not been created
 * or cannot be accessed.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class MissingDatabaseException extends Exception {

    /**
     * Constructs a new {@code MissingDatabaseException} with a message
     * indicating the missing database name.
     *
     * @param databaseName the name of the database that was not found
     */
    public MissingDatabaseException(String databaseName) {
        super("Database '" + databaseName + "' does not exist.");
    }
}