package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import com.appiancorp.solutionsconsulting.cs.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;

public class UpdateOneOperation extends CollectionWriteOperation {
    private String outputType;
    private String filterJson;
    private String updateJson;
    private Boolean skipDateTimeConversion;
    private Document filterDoc;
    private Document updateDoc;

    public UpdateOneOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String filterJson, String updateJson, Boolean skipDateTimeConversion
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setFilterJson(filterJson);
        setUpdateJson(updateJson);
        setSkipDateTimeConversion(skipDateTimeConversion);

        try {
            setFilterDoc(MongoDocumentUtil.prepDocumentForInsert(Document.parse(filterJson), getSkipDateTimeConversion()));
            setUpdateDoc(MongoDocumentUtil.prepDocumentForInsert(Document.parse(updateJson), getSkipDateTimeConversion()));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Insert One JSON: Invalid JSON provided.",
                    filterJson);
        }
    }


    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Insert One JSON", getFilterJson());

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

    public Document getFilterDoc() {
        return filterDoc;
    }

    public void setFilterDoc(Document filterDoc) {
        this.filterDoc = filterDoc;
    }

    public Boolean getSkipDateTimeConversion() {
        return skipDateTimeConversion;
    }

    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }

    public String getUpdateJson() {
        return updateJson;
    }

    public void setUpdateJson(String updateJson) {
        this.updateJson = updateJson;
    }

    public Document getUpdateDoc() {
        return updateDoc;
    }

    public void setUpdateDoc(Document updateDoc) {
        this.updateDoc = updateDoc;
    }
}
