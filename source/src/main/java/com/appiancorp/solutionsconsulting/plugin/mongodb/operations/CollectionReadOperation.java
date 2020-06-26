package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.Map;


/**
 * Data object class for read operations on Collections
 */
public class CollectionReadOperation extends BaseOperation {
    private String databaseName;
    private Boolean validateDatabase;
    private String collectionName;
    private Boolean validateCollection;
    private String readPreference;
    private String readConcern;


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

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Boolean getValidateDatabase() {
        return validateDatabase;
    }

    public void setValidateDatabase(Boolean validateDatabase) {
        this.validateDatabase = validateDatabase;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Boolean getValidateCollection() {
        return validateCollection;
    }

    public void setValidateCollection(Boolean validateCollection) {
        this.validateCollection = validateCollection;
    }

    public String getReadPreference() {
        return readPreference;
    }

    public void setReadPreference(String readPreference) {
        this.readPreference = readPreference;
    }

    public String getReadConcern() {
        return readConcern;
    }

    public void setReadConcern(String readConcern) {
        this.readConcern = readConcern;
    }
}
