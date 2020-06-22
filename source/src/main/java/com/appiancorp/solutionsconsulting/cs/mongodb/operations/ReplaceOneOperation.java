package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import com.appiancorp.solutionsconsulting.cs.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;

public class ReplaceOneOperation extends CollectionWriteOperation {
    private String outputType;
    private String filterJson;
    private String replacementJson;
    private Boolean skipDateTimeConversion;
    private Document filterDocument;
    private Document replacementDocument;

    public ReplaceOneOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String filterJson, String replacementJson, Boolean skipDateTimeConversion
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setFilterJson(filterJson);
        setReplacementJson(replacementJson);
        setSkipDateTimeConversion(skipDateTimeConversion);

        try {
            setFilterDocument(Document.parse(filterJson));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Replace One JSON: Invalid Filter JSON provided.",
                    filterJson);
        }
        try {
            setReplacementDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(replacementJson), getSkipDateTimeConversion()));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Replace One JSON: Invalid Replacement JSON provided.",
                    replacementJson);
        }
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Filter JSON", getFilterJson());
        diagnostic.put("Replace One JSON", getReplacementJson());

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

    public String getReplacementJson() {
        return replacementJson;
    }

    public void setReplacementJson(String replacementJson) {
        this.replacementJson = replacementJson;
    }

    public Boolean getSkipDateTimeConversion() {
        return skipDateTimeConversion;
    }

    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }

    public Document getFilterDocument() {
        return filterDocument;
    }

    public void setFilterDocument(Document filterDocument) {
        this.filterDocument = filterDocument;
    }

    public Document getReplacementDocument() {
        return replacementDocument;
    }

    public void setReplacementDocument(Document replacementDocument) {
        this.replacementDocument = replacementDocument;
    }
}