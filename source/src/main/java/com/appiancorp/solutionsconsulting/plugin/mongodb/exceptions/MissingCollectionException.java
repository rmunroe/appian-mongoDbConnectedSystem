package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

/**
 * Thrown to indicate that the specified MongoDB collection does not exist.
 *
 * <p>This exception is typically used when an operation requires a
 * valid collection name, but the provided collection either has not
 * been created or is unreachable.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class MissingCollectionException extends Exception {

    /**
     * Constructs a new <code>MissingCollectionException</code> with a message
     * that identifies the missing collection.
     *
     * @param collectionName the name of the collection that was not found
     */
    public MissingCollectionException(String collectionName) {
        super("Collection '" + collectionName + "' does not exist.");
    }
}