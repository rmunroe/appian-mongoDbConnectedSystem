package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InsertManyOperation extends CollectionWriteOperation {
    private String outputType;
    private String jsonArray;
    private List<Document> documents;

    public InsertManyOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String jsonArray
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setJsonArray(jsonArray);

        List<Document> documents = new ArrayList<>();
        try {
            if (jsonArray.matches("^\\[.*]$")) {
                @SuppressWarnings("unchecked")
                List<Document> stages = (List<Document>) Document.parse("{docs:" + jsonArray + "}").get("docs");
                documents.addAll(stages);
            } else {
                throw new InvalidJsonException(
                        "Insert Many JSONs: JSON provided does not appear to be an ARRAY of Documents.",
                        jsonArray);
            }
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Insert Many JSONs: Invalid JSON provided.",
                    jsonArray);
        }
        setDocuments(documents);
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Insert Many JSON", getJsonArray());

        return diagnostic;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(String jsonArray) {
        this.jsonArray = jsonArray;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
