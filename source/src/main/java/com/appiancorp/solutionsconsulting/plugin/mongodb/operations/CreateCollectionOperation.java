package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.HashMap;
import java.util.Map;


public class CreateCollectionOperation implements Operation {
    private String databaseName;
    private Boolean validateDatabase;
    private String collectionName;

    public CreateCollectionOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName
    ) {
        setDatabaseName(databaseName);
        setValidateDatabase(validateDatabase);
        setCollectionName(collectionName);
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = new HashMap<>();

        diagnostic.put("Database", getDatabaseName());
        diagnostic.put("Validate Database", getValidateDatabase());
        diagnostic.put("Collection", getCollectionName());

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
}
