package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.Map;


public class DropCollectionOperation extends CollectionWriteOperation {

    public DropCollectionOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection
    ) {
        super(databaseName, validateDatabase, collectionName, validateCollection);
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        return super.getRequestDiagnostic();
    }
}
