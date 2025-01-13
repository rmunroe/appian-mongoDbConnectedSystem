package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.Map;

/**
 * Represents a write-based operation in the MongoDB plugin focused on a specific collection.
 * <p>
 * This class extends {@link BaseOperation}, which implements the {@link Operation} interface,
 * and defines the base context for performing write operations against a MongoDB collection.
 * Use this class as a foundation for operations that require a particular database and collection,
 * as well as flags indicating whether these entities should be validated prior to execution.
 * </p>
 * <p>
 * Subclasses of {@code CollectionWriteOperation} typically add more specific behaviors (e.g.,
 * bulk writes, updates, inserts, deletes) while using the fields and methods defined here to
 * properly track and report details such as database name and collection name.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class CollectionWriteOperation extends BaseOperation {

    /**
     * The name of the MongoDB database on which the operation will be performed.
     */
    private String databaseName;

    /**
     * Indicates whether the specified {@code databaseName} should be validated
     * (e.g., by checking its existence) before the operation is executed.
     */
    private Boolean validateDatabase;

    /**
     * The name of the MongoDB collection on which the operation will be performed.
     */
    private String collectionName;

    /**
     * Indicates whether the specified {@code collectionName} should be validated
     * (e.g., by checking its existence) before the operation is executed.
     */
    private Boolean validateCollection;

    /**
     * Constructs a {@code CollectionWriteOperation} with the specified database and collection context.
     *
     * @param databaseName       the name of the database to target
     * @param validateDatabase   whether the database should be validated prior to running the operation
     * @param collectionName     the name of the collection to target
     * @param validateCollection whether the collection should be validated prior to running the operation
     */
    public CollectionWriteOperation(
            String databaseName,
            Boolean validateDatabase,
            String collectionName,
            Boolean validateCollection
    ) {
        setDatabaseName(databaseName);
        setValidateDatabase(validateDatabase);
        setCollectionName(collectionName);
        setValidateCollection(validateCollection);
    }

    /**
     * Provides diagnostic information specific to this collection-centric operation.
     * <p>
     * In addition to retrieving a base diagnostic map from {@link BaseOperation#getRequestDiagnostic()},
     * this method adds details such as the {@code databaseName}, {@code validateDatabase},
     * {@code collectionName}, and {@code validateCollection} settings.
     * </p>
     *
     * @return a map of key-value pairs that describe the database and collection context for this operation
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Database", getDatabaseName());
        diagnostic.put("Validate Database", getValidateDatabase());
        diagnostic.put("Collection", getCollectionName());
        diagnostic.put("Validate Collection", getValidateCollection());

        return diagnostic;
    }

    /**
     * Returns the name of the database on which the collection operations will be performed.
     *
     * @return the database name
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Sets the name of the database on which the collection operations will be performed.
     *
     * @param databaseName the database name to be used
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Indicates whether the plugin should validate the specified database before performing the operation.
     *
     * @return {@code true} if the database should be validated; {@code false} otherwise
     */
    public Boolean getValidateDatabase() {
        return validateDatabase;
    }

    /**
     * Determines whether the plugin should validate the specified database before performing the operation.
     *
     * @param validateDatabase {@code true} to validate the database; {@code false} otherwise
     */
    public void setValidateDatabase(Boolean validateDatabase) {
        this.validateDatabase = validateDatabase;
    }

    /**
     * Returns the name of the MongoDB collection on which the operation will be performed.
     *
     * @return the collection name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the name of the MongoDB collection on which the operation will be performed.
     *
     * @param collectionName the collection name to be used
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * Indicates whether the plugin should validate the specified collection before performing the operation.
     *
     * @return {@code true} if the collection should be validated; {@code false} otherwise
     */
    public Boolean getValidateCollection() {
        return validateCollection;
    }

    /**
     * Determines whether the plugin should validate the specified collection before performing the operation.
     *
     * @param validateCollection {@code true} to validate the collection; {@code false} otherwise
     */
    public void setValidateCollection(Boolean validateCollection) {
        this.validateCollection = validateCollection;
    }
}