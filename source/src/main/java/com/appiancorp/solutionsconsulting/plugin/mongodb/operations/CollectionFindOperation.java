package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.Map;


public class CollectionFindOperation extends CollectionReadOperation {
    private String outputType;
    private String filterJson;
    private Document filterDocument;
    private String sortJson;
    private Document sortDocument;
    private String projectionJson;
    private Document projectionDocument;
    private Integer limit;
    private Integer skip;
    private Boolean includeRecordId;
    private Integer maxTime;
    private Collation collation;


    public CollectionFindOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String readPreference, String readConcern,
            String outputType, String filterJson, String sortJson, String projectionJson,
            Integer limit, Integer skip, Boolean includeRecordId, Integer maxTime, Collation collation
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection, readPreference, readConcern);

        setOutputType(outputType);

        setFilterJson(filterJson);
        if (StringUtils.isNotEmpty(getFilterJson()))
            try {
                setFilterDocument(Document.parse(getFilterJson()));
            } catch (Exception ex) {
                throw new InvalidJsonException(
                        "Filter JSON Query: Invalid JSON provided.",
                        getFilterJson());
            }

        setSortJson(sortJson);
        if (StringUtils.isNotEmpty(getSortJson()))
            try {
                setSortDocument(Document.parse(getSortJson()));
            } catch (Exception ex) {
                throw new InvalidJsonException(
                        "Sort JSON: Invalid JSON provided.",
                        getSortJson());
            }

        setProjectionJson(projectionJson);
        if (StringUtils.isNotEmpty(getProjectionJson()))
            try {
                setProjectionDocument(Document.parse(getProjectionJson()));
            } catch (Exception ex) {
                throw new InvalidJsonException(
                        "Projection JSON: Invalid JSON provided.",
                        getProjectionJson());
            }


        setLimit(limit);
        setSkip(skip);
        setIncludeRecordId(includeRecordId);
        setMaxTime(maxTime);

        setCollation(collation);
    }

    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Query Filter JSON", getFilterJson());
        diagnostic.put("Sort JSON", getSortJson());
        diagnostic.put("Projection JSON", getProjectionJson());
        diagnostic.put("Limit", getLimit());
        diagnostic.put("Skip", getSkip());
        diagnostic.put("Include Record Id", getIncludeRecordId());
        diagnostic.put("Max Processing Time", getMaxTime());
        diagnostic.put("Collation", (getCollation() == null) ? null : getCollation().asDocument().toJson());

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

    public String getSortJson() {
        return sortJson;
    }

    public void setSortJson(String sortJson) {
        this.sortJson = sortJson;
    }

    public Document getSortDocument() {
        return sortDocument;
    }

    public void setSortDocument(Document sortDocument) {
        this.sortDocument = sortDocument;
    }

    public String getProjectionJson() {
        return projectionJson;
    }

    public void setProjectionJson(String projectionJson) {
        this.projectionJson = projectionJson;
    }

    public Document getProjectionDocument() {
        return projectionDocument;
    }

    public void setProjectionDocument(Document projectionDocument) {
        this.projectionDocument = projectionDocument;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Boolean getIncludeRecordId() {
        return includeRecordId;
    }

    public void setIncludeRecordId(Boolean includeRecordId) {
        this.includeRecordId = includeRecordId;
    }

    public Integer getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(Integer maxTime) {
        this.maxTime = maxTime;
    }

    public Collation getCollation() {
        return collation;
    }

    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}