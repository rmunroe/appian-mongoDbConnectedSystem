package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.util.Map;


public class CollectionCountOperation extends CollectionOperation {
    private String filterJson;
    private Document filterDocument;
    private Collation collation;


    public CollectionCountOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String readPreference, String readConcern,
            String filterJson, Collation collation
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection, readPreference, readConcern);

        setFilterJson(filterJson);
        if (StringUtils.isNotEmpty(getFilterJson()))
            try {
                setFilterDocument(Document.parse(getFilterJson()));
            } catch (Exception ex) {
                throw new InvalidJsonException(
                        "Find JSON Query: Invalid JSON provided. Perhaps you need to call a!toJson() on your value?",
                        getFilterJson());
            }

        setCollation(collation);
    }

    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Query Filter JSON", getFilterJson());
        diagnostic.put("Collation", (getCollation() == null) ? null : getCollation().asDocument().toJson());

        return diagnostic;
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

    public Collation getCollation() {
        return collation;
    }

    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}