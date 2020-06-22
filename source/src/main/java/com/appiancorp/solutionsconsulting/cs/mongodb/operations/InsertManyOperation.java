package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.INSERT_SOURCE_DOCUMENT;

public class InsertManyOperation extends CollectionWriteOperation {

    private String outputType;
    private String insertSource;
    private com.appian.connectedsystems.templateframework.sdk.configuration.Document sourceFile;
    private Boolean fileIsArray;
    private String jsonArray;
    private List<Document> documents;

    public InsertManyOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String insertSource,
            com.appian.connectedsystems.templateframework.sdk.configuration.Document sourceFile, Boolean fileIsArray,
            String sourceJsonArray
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setInsertSource(insertSource);
        setSourceFile(sourceFile);
        setFileIsArray(fileIsArray);


        if (insertSource.equals(INSERT_SOURCE_DOCUMENT)) {
            InputStream inputStream = sourceFile.getInputStream();
            if (fileIsArray) {
                // Read entire file contents into JSON array
                sourceJsonArray = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining(""));
            } else {
                // Create a single JSON array from the file contents
                sourceJsonArray = "[" + new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining(", ")) + "]";
            }
        }

        setJsonArray(sourceJsonArray);


        List<Document> documents = new ArrayList<>();
        if (sourceJsonArray.matches("^\\[.*]$")) {
            try {

                @SuppressWarnings("unchecked")
                List<Document> doc = (List<Document>) Document.parse("{docs:" + sourceJsonArray + "}").get("docs");
                documents.addAll(doc);
            } catch (Exception ex) {
                throw new InvalidJsonException(
                        "Insert Many JSONs: Invalid JSON provided.",
                        sourceJsonArray);
            }
        } else {
            throw new InvalidJsonException(
                    "Insert Many JSONs: JSON provided does not appear to be an ARRAY of Documents.",
                    sourceJsonArray);
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

    public String getInsertSource() {
        return insertSource;
    }

    public void setInsertSource(String insertSource) {
        this.insertSource = insertSource;
    }

    public com.appian.connectedsystems.templateframework.sdk.configuration.Document getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(com.appian.connectedsystems.templateframework.sdk.configuration.Document sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Boolean getFileIsArray() {
        return fileIsArray;
    }

    public void setFileIsArray(Boolean fileIsArray) {
        this.fileIsArray = fileIsArray;
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
