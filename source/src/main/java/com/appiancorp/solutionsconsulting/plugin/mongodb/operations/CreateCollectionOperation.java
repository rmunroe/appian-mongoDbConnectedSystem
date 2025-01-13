package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents an operation for creating a collection within a specified MongoDB database.
 * This class implements the {@link Operation} interface, providing diagnostic information
 * about the operation through the {@link #getRequestDiagnostic()} method.
 *
 * <p>
 * The {@code CreateCollectionOperation} is designed to encapsulate the metadata required
 * to create a new collection, including the database name, an optional flag to validate
 * the presence of the database, and the collection name.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class CreateCollectionOperation implements Operation {
    private String databaseName;
    private Boolean validateDatabase;
    private String collectionName;

    /**
     * Constructs a new instance of CreateCollectionOperation, representing an operation
     * to create a collection in a specified MongoDB database. This operation allows for
     * specifying the database name, whether to validate the database existence, and the
     * collection name to be created.
     *
     * @param databaseName      the name of the database where the collection will be created
     * @param validateDatabase  a flag indicating whether to validate the existence of the database
     * @param collectionName    the name of the collection to be created in the specified database
     */
    public CreateCollectionOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName
    ) {
        setDatabaseName(databaseName);
        setValidateDatabase(validateDatabase);
        setCollectionName(collectionName);
    }


    /**
     * Retrieves diagnostic information related to the create collection operation.
     * The method provides details about the operation, encapsulated in a map
     * containing key-value pairs that describe the specific configuration or metadata
     * of the operation, such as the database name, validation settings, and collection name.
     *
     * @return a Map where the keys are Strings representing the diagnostic identifiers
     *         ("Database", "Validate Database", "Collection") and the values are
     *         Objects detailing the respective settings or metadata of the operation
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = new HashMap<>();

        diagnostic.put("Database", getDatabaseName());
        diagnostic.put("Validate Database", getValidateDatabase());
        diagnostic.put("Collection", getCollectionName());

        return diagnostic;
    }

    /**
     * Retrieves the name of the database associated with the operation.
     *
     * @return the database name as a {@code String}
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Sets the name of the MongoDB database for the operation.
     *
     * @param databaseName the name of the database to be used in the operation
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Retrieves the current state of the validateDatabase flag.
     *
     * @return a {@code Boolean} indicating whether the database's presence should be validated.
     *         Returns {@code true} if validation is enabled, {@code false} otherwise.
     */
    public Boolean getValidateDatabase() {
        return validateDatabase;
    }

    /**
     * Sets the flag indicating whether the presence of the database should be validated.
     *
     * @param validateDatabase a Boolean value where {@code true} indicates that validation
     *                         of the database presence is required, and {@code false} indicates
     *                         that no validation is necessary.
     */
    public void setValidateDatabase(Boolean validateDatabase) {
        this.validateDatabase = validateDatabase;
    }

    /**
     * Retrieves the name of the collection associated with the operation.
     *
     * @return the name of the collection as a String
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the name of the MongoDB collection for the operation.
     *
     * @param collectionName the name of the collection to be used in the operation
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
