package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import com.appiancorp.solutionsconsulting.cs.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;

public class InsertOneOperation extends CollectionWriteOperation {
    private String outputType;
    private String json;
    private Document document;

    public InsertOneOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String json
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setJson(json);

        try {
            setDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(json)));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Insert One JSON: Invalid JSON provided.",
                    json);
        }
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Insert One JSON", getJson());

        return diagnostic;
    }


    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
