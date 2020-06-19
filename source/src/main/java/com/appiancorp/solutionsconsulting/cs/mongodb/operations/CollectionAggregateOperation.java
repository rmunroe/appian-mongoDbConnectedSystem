package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CollectionAggregateOperation extends CollectionReadOperation {
    private String outputType;
    private String stagesJson;
    private List<Document> stagesDocuments;
    private Collation collation;


    public CollectionAggregateOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String readPreference, String readConcern,
            String outputType, String stagesJson, Collation collation
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection, readPreference, readConcern);

        setOutputType(outputType);
        setStagesJson(stagesJson);

        List<Document> stagesDocuments = new ArrayList<>();
        try {
            if (stagesJson.matches("^\\[.*]$")) {
                @SuppressWarnings("unchecked")
                List<Document> stages = (List<Document>) Document.parse("{stages:" + stagesJson + "}").get("stages");
                stagesDocuments.addAll(stages);
            } else {
                throw new InvalidJsonException(
                        "Aggregate Pipeline Stages JSONs: JSON provided does not appear to be an ARRAY of Documents.",
                        stagesJson);
            }
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Aggregate Pipeline Stages JSONs: Invalid JSON provided.",
                    stagesJson);
        }
        setStagesDocuments(stagesDocuments);

        setCollation(collation);
    }

    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Aggregate Pipeline Stages JSON", getStagesJson());

        diagnostic.put("Collation", (getCollation() == null) ? null : getCollation().asDocument().toJson());

        return diagnostic;
    }


    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getStagesJson() {
        return stagesJson;
    }

    public void setStagesJson(String stagesJson) {
        this.stagesJson = stagesJson;
    }

    public List<Document> getStagesDocuments() {
        return stagesDocuments;
    }

    public void setStagesDocuments(List<Document> stagesDocuments) {
        this.stagesDocuments = stagesDocuments;
    }

    public Collation getCollation() {
        return collation;
    }

    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}