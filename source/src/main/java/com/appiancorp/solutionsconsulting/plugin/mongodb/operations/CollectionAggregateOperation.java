package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Represents an aggregate operation on a MongoDB collection.
 *
 * <p>
 * This class extends the functionality of {@code CollectionReadOperation}
 * to execute MongoDB aggregation pipelines on a specified collection.
 * It supports specifying aggregation stages as a JSON string, output type,
 * and a collation for locale-specific string comparisons.
 * <p>
 *
 * </p>
 * The provided JSON for the aggregation stages is validated to ensure it
 * conforms to the expected format (an array of documents).
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class CollectionAggregateOperation extends CollectionReadOperation {
    private String outputType;
    private String stagesJson;
    private List<Document> stagesDocuments;
    private Collation collation;

    /**
     * Constructs a new {@code CollectionAggregateOperation} to perform an aggregation operation on a specified database
     * collection. This operation allows users to define aggregation pipelines and supports additional configuration options.
     *
     * @param databaseName       the name of the MongoDB database to be accessed
     * @param validateDatabase   flag indicating whether to validate the presence of the specified database
     * @param collectionName     the name of the collection within the database to perform the aggregate operation
     * @param validateCollection flag indicating whether to validate the presence of the specified collection
     * @param readPreference     the read preference used for the operation, which determines the replica set member to query
     * @param readConcern        the read concern level specifying the durability of data being read
     * @param outputType         the type of output expected from the aggregation operation
     * @param stagesJson         a JSON array string representation of the aggregation pipeline stages
     * @param collation          the collation options for the aggregation operation, used to specify language-specific rules for string comparison
     * @throws InvalidJsonException if the provided {@code stagesJson} string is not valid JSON or does not represent an array of documents
     */
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
            if (stagesJson.startsWith("[") && stagesJson.endsWith("]")) {
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

    /**
     * Retrieves diagnostic information for the aggregate operation request.
     * This method extends the diagnostics data provided by the superclass
     * with additional information specific to the aggregate operation, including
     * output type, aggregate pipeline stages, and collation details.
     *
     * @return a {@link Map} containing diagnostic key-value pairs, where keys represent
     *         diagnostic data categories (like "Output Type", "Aggregate Pipeline Stages JSON",
     *         and "Collation") and values provide the corresponding details
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Aggregate Pipeline Stages JSON", getStagesJson());

        diagnostic.put("Collation", (getCollation() == null) ? null : getCollation().asDocument().toJson());

        return diagnostic;
    }


    /**
     * Retrieves the output type for the operation.
     *
     * @return a {@code String} representing the type of output produced by the operation
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the type of output for the operation.
     *
     * @param outputType the desired output type, typically indicating
     *                   the format or representation of results.
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * Retrieves the JSON string representing the stages of the collection aggregation operation.
     *
     * @return a {@code String} containing the stages of the collection aggregation in JSON format
     */
    public String getStagesJson() {
        return stagesJson;
    }

    /**
     * Sets the JSON representation of the aggregation stages for the operation.
     *
     * @param stagesJson a string containing the JSON representation of the aggregation stages
     */
    public void setStagesJson(String stagesJson) {
        this.stagesJson = stagesJson;
    }

    /**
     * Retrieves the list of Document objects representing the stages of the aggregation operation.
     *
     * @return a List of Document objects corresponding to the aggregation stages
     */
    public List<Document> getStagesDocuments() {
        return stagesDocuments;
    }

    /**
     * Sets the list of stages documents for the aggregation operation.
     *
     * @param stagesDocuments the list of {@link Document} objects representing
     *                        the stages of the aggregation pipeline to be executed
     */
    public void setStagesDocuments(List<Document> stagesDocuments) {
        this.stagesDocuments = stagesDocuments;
    }

    /**
     * Retrieves the collation settings used for the aggregation operation.
     *
     * @return the {@link Collation} instance representing the collation settings,
     *         or null if no collation has been set.
     */
    public Collation getCollation() {
        return collation;
    }

    /**
     * Sets the collation configuration for the operation.
     *
     * <p>
     * Collation defines language-specific rules for string comparison, such as
     * rules for letter case and accent marks. Setting a collation allows
     * customized comparison logic to be applied to string fields in the database
     * operation.
     * </p>
     *
     * @param collation the {@link Collation} to be assigned to the operation
     */
    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}