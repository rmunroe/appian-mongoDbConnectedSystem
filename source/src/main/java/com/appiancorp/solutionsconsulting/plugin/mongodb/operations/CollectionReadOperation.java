package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.Map;


/**
 * Represents an operation to read data from a specific collection in a database.
 * This class includes configuration parameters such as database and collection validation,
 * read preference, and read concern to control how the read operation is executed.
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class CollectionReadOperation extends BaseOperation {
    private String databaseName;
    private Boolean validateDatabase;
    private String collectionName;
    private Boolean validateCollection;
    private String readPreference;
    private String readConcern;


    /**
     * Constructs a new CollectionReadOperation instance with the specified parameters.
     *
     * @param databaseName       the name of the database on which the operation will be performed
     * @param validateDatabase   a boolean indicating whether the database name validation is required
     * @param collectionName     the name of the collection on which the operation will be performed
     * @param validateCollection a boolean indicating whether the collection name validation is required
     * @param readPreference     the read preference for the operation (e.g., primary, secondary)
     * @param readConcern        the read concern level for the operation (e.g., local, majority)
     */
    public CollectionReadOperation(String databaseName, Boolean validateDatabase,
                                   String collectionName, Boolean validateCollection,
                                   String readPreference, String readConcern
    ) {
        setDatabaseName(databaseName);
        setValidateDatabase(validateDatabase);
        setCollectionName(collectionName);
        setValidateCollection(validateCollection);
        setReadPreference(readPreference);
        setReadConcern(readConcern);
    }

    /**
     * Retrieves diagnostic information for the current MongoDB collection read operation.
     * This method extends the base diagnostic data provided by the superclass by including
     * additional information specific to the collection read operation, such as the database name,
     * collection name, and read preferences.
     *
     * @return a {@link Map} containing key-value pairs representing diagnostic data for the
     *         collection read operation, including database name, collection name, validation flags,
     *         read preference, and read concern.
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Database", getDatabaseName());
        diagnostic.put("Validate Database", getValidateDatabase());
        diagnostic.put("Collection", getCollectionName());
        diagnostic.put("Validate Collection", getValidateDatabase());
        diagnostic.put("Read Preference", getReadPreference());
        diagnostic.put("Read Concern", getReadConcern());

        return diagnostic;
    }

    /**
     * Retrieves the name of the database associated with the operation.
     *
     * @return the name of the database as a String
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Sets the name of the database for the operation.
     *
     * @param databaseName the name of the database to be used
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Retrieves the flag indicating whether the database name should be validated.
     *
     * @return a {@code Boolean} specifying whether the database name validation is required.
     */
    public Boolean getValidateDatabase() {
        return validateDatabase;
    }

    /**
     * Sets the validateDatabase property, which determines whether database validation
     * should be performed during operations.
     *
     * @param validateDatabase a Boolean value indicating whether to enable or disable
     *                         database validation
     */
    public void setValidateDatabase(Boolean validateDatabase) {
        this.validateDatabase = validateDatabase;
    }

    /**
     * Retrieves the name of the collection associated with the current operation.
     *
     * @return the collection name as a String
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Sets the name of the collection to be used in the operation.
     *
     * @param collectionName the name of the collection
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * Retrieves the validation status of the collection.
     *
     * @return a Boolean indicating whether the collection validation is enabled.
     *         Returns true if validation is enabled, false otherwise.
     */
    public Boolean getValidateCollection() {
        return validateCollection;
    }

    /**
     * Sets the flag that determines whether the collection should be validated.
     *
     * @param validateCollection a Boolean value indicating whether collection validation is enabled.
     *                            If true, the collection will be validated before performing operations.
     */
    public void setValidateCollection(Boolean validateCollection) {
        this.validateCollection = validateCollection;
    }

    /**
     * Retrieves the read preference for the operation.
     *
     * @return the read preference as a String
     */
    public String getReadPreference() {
        return readPreference;
    }

    /**
     * Sets the read preference for the operation.
     *
     * @param readPreference the preferred read mode for the operation. This typically
     *                        specifies how MongoDB directs queries to replica set members.
     */
    public void setReadPreference(String readPreference) {
        this.readPreference = readPreference;
    }

    /**
     * Retrieves the read concern value for this operation.
     *
     * @return a string representing the read concern, which determines the level
     *         of isolation for read operations.
     */
    public String getReadConcern() {
        return readConcern;
    }

    /**
     * Sets the read concern level for the MongoDB operation.
     *
     * @param readConcern the read concern level to be applied, which determines the
     *                    level of isolation for read operations, such as "local",
     *                    "majority", or other MongoDB-supported levels
     */
    public void setReadConcern(String readConcern) {
        this.readConcern = readConcern;
    }
}
