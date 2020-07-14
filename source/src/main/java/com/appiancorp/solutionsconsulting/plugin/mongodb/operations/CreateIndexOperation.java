package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;


public class CreateIndexOperation extends CollectionWriteOperation {
    private String indexJson;
    private Document indexDocument;


    public CreateIndexOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String indexJson
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setIndexJson(indexJson);

        try {
            setIndexDocument(Document.parse(indexJson));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Index JSON: Invalid JSON provided.",
                    indexJson);
        }
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Index JSON", getIndexJson());
        return diagnostic;
    }

    public String getIndexJson() {
        return indexJson;
    }

    public void setIndexJson(String indexJson) {
        this.indexJson = indexJson;
    }

    public Document getIndexDocument() {
        return indexDocument;
    }

    public void setIndexDocument(Document indexDocument) {
        this.indexDocument = indexDocument;
    }
}
