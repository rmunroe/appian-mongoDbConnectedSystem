package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.bson.Document;

import java.util.Map;


public class DeleteOperation extends CollectionWriteOperation {
    private String outputType;
    private String filterJson;
    private Collation collation;

    private Document filterDocument;
    private Document updateDocument;

    public DeleteOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String filterJson, Collation collation
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setFilterJson(filterJson);
        setCollation(collation);

        try {
            setFilterDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(getFilterJson()), false));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Delete JSON: Invalid JSON provided.",
                    getFilterJson());
        }
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Filter JSON", getFilterJson());

        return diagnostic;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getFilterJson() {
        return filterJson;
    }

    public void setFilterJson(String filterJson) {
        this.filterJson = filterJson;
    }

    public Document getFilterDocument() {
        return filterDocument;
    }

    public void setFilterDocument(Document filterDocument) {
        this.filterDocument = filterDocument;
    }

    public Document getUpdateDocument() {
        return updateDocument;
    }

    public void setUpdateDocument(Document updateDocument) {
        this.updateDocument = updateDocument;
    }

    public Collation getCollation() {
        return collation;
    }

    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}
