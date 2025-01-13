package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.Map;


/**
 * Represents an operation for dropping a MongoDB collection.
 *
 * <p>
 * This class extends {@code CollectionWriteOperation}, leveraging its properties
 * and methods to provide a context-specific implementation for dropping a
 * collection from a specified database in MongoDB. It utilizes the database name,
 * collection name, and corresponding validation flags to ensure proper initialization.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class DropCollectionOperation extends CollectionWriteOperation {

    /**
     * Constructs a {@code DropCollectionOperation} with the specified database and collection context.
     *
     * @param databaseName       the name of the database to target
     * @param validateDatabase   whether the database should be validated prior to executing the operation
     * @param collectionName     the name of the collection to be dropped
     * @param validateCollection whether the collection should be validated prior to executing the operation
     */
    public DropCollectionOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection
    ) {
        super(databaseName, validateDatabase, collectionName, validateCollection);
    }


    /**
     * Retrieves diagnostic information specific to the drop collection operation.
     *
     * <p>
     * This method provides a map of key-value pairs detailing the context of
     * the operation, including the database and collection-related settings.
     * The implementation here inherits and returns the diagnostic information
     * prepared by the parent class.
     * </p>
     *
     * @return a map of diagnostic data for the drop collection operation
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        return super.getRequestDiagnostic();
    }
}
