package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import java.util.Map;


/**
 * Data object class for write operations on Collections
 */
public class CollectionWriteOperation extends BaseOperation {
    private String databaseName;
    private Boolean validateDatabase;
    private String collectionName;
    private Boolean validateCollection;

    public CollectionWriteOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection
    ) {
        setDatabaseName(databaseName);
        setValidateDatabase(validateDatabase);
        setCollectionName(collectionName);
        setValidateCollection(validateCollection);
    }

    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Database", getDatabaseName());
        diagnostic.put("Validate Database", getValidateDatabase());
        diagnostic.put("Collection", getCollectionName());
        diagnostic.put("Validate Collection", getValidateDatabase());

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
}
