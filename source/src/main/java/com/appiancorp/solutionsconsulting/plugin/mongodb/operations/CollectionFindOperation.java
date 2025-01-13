package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.Map;


/**
 * Represents a find operation on a collection. This class extends the base functionality of
 * {@link CollectionReadOperation} while introducing parameters that allow filtering, sorting,
 * projecting, and other customization of the read operation.
 * <p>
 * A find operation retrieves documents from a collection based on specified criteria like filters,
 * projections, sorting, and limits.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
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


    /**
     * Constructs a new instance of the {@code CollectionFindOperation} class for querying
     * a MongoDB collection with various configuration options such as filters, sorting, projection,
     * and additional execution settings.
     *
     * @param databaseName       The name of the database to execute the query against.
     * @param validateDatabase   A flag indicating whether the database name should be validated.
     * @param collectionName     The name of the collection to execute the query against.
     * @param validateCollection A flag indicating whether the collection name should be validated.
     * @param readPreference     The read preference to use for reading data from the collection.
     * @param readConcern        The read concern level that determines the consistency level for the read operation.
     * @param outputType         The format in which the results should be returned.
     * @param filterJson         A JSON string that defines the query filter criteria.
     * @param sortJson           A JSON string that specifies the sort order for the query results.
     * @param projectionJson     A JSON string to specify which fields of the documents should be returned.
     * @param limit              The maximum number of documents to return from the query.
     * @param skip               The number of documents to skip before returning results.
     * @param includeRecordId    A flag indicating whether the `recordId` of documents should be included in the results.
     * @param maxTime            The maximum execution time in milliseconds for the operation.
     * @param collation          The collation configuration allowing locale-specific string comparisons.
     * @throws InvalidJsonException If the provided JSON strings for filter, sort, or projection are invalid or cannot be parsed.
     */
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

    /**
     * Retrieves diagnostic information for the current "Collection Find Operation."
     * This method provides a map containing details about specific parameters
     * and configurations used in executing the operation, building upon the
     * diagnostics provided by the superclass implementation.
     *
     * @return a {@link Map} containing key-value pairs including operation-specific
     *         diagnostic data such as output type, query filter, sort configuration,
     *         projection configuration, limit, skip, record inclusion preferences,
     *         max processing time, and collation settings.
     */
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

    /**
     * Retrieves the output type configured for the operation.
     *
     * @return the output type as a String
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the output type for the operation.
     *
     * @param outputType the output type to be set, typically determining the format
     *                   of the output result, such as JSON or a specific document structure.
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * Retrieves the filter JSON string used for the MongoDB find or delete operation.
     *
     * <p>
     * The filter JSON represents the query criteria in a serialized JSON format
     * that is applied to MongoDB queries to specify which documents should be
     * included in the operation.
     * </p>
     *
     * @return the filter JSON string that defines the query criteria.
     */
    public String getFilterJson() {
        return filterJson;
    }

    /**
     * Sets the filter criteria in JSON format for the operation.
     *
     * @param filterJson the JSON string representing the filter to be applied
     */
    public void setFilterJson(String filterJson) {
        this.filterJson = filterJson;
    }

    /**
     * Retrieves the filter document associated with the collection operation.
     *
     * @return a {@link Document} representing the filter criteria for this operation
     */
    public Document getFilterDocument() {
        return filterDocument;
    }

    /**
     * Sets the filter document for the find operation.
     * The filter document specifies the criteria that determine which documents
     * should be retrieved during the operation.
     *
     * @param filterDocument the {@link Document} representing the filter criteria
     */
    public void setFilterDocument(Document filterDocument) {
        this.filterDocument = filterDocument;
    }

    /**
     * Retrieves the sort criteria as a JSON string.
     *
     * @return a string containing the JSON representation of the sort criteria
     */
    public String getSortJson() {
        return sortJson;
    }

    /**
     * Sets the JSON string representation of the sort criteria for a MongoDB query.
     *
     * @param sortJson the JSON string defining the sort criteria
     */
    public void setSortJson(String sortJson) {
        this.sortJson = sortJson;
    }

    /**
     * Retrieves the document used for sorting within the query operation.
     *
     * @return the {@link Document} object representing the sorting criteria.
     */
    public Document getSortDocument() {
        return sortDocument;
    }

    /**
     * Sets the sort document to define the sort criteria for a MongoDB query.
     *
     * @param sortDocument a Document containing the fields and sorting order to apply to the query results
     */
    public void setSortDocument(Document sortDocument) {
        this.sortDocument = sortDocument;
    }

    /**
     * Retrieves the JSON string representing the projection criteria for a MongoDB query.
     *
     * @return a string containing the projection JSON, which defines the fields
     *         to include or exclude in the query results.
     */
    public String getProjectionJson() {
        return projectionJson;
    }

    /**
     * Sets the projection JSON for the MongoDB query.
     *
     * @param projectionJson the JSON string representing the projection criteria
     */
    public void setProjectionJson(String projectionJson) {
        this.projectionJson = projectionJson;
    }

    /**
     * Retrieves the BSON document used to specify the fields to include or exclude in the query results.
     *
     * @return the projection document used in the query, represented as a {@link Document}
     */
    public Document getProjectionDocument() {
        return projectionDocument;
    }

    /**
     * Sets the projection document to specify the desired fields included or excluded in the query results.
     *
     * @param projectionDocument the {@link Document} specifying the projection criteria
     */
    public void setProjectionDocument(Document projectionDocument) {
        this.projectionDocument = projectionDocument;
    }

    /**
     * Retrieves the value of the limit parameter for a collection operation.
     *
     * @return the limit as an Integer, representing the maximum number of results to fetch; may be null if no limit is set
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * Sets the maximum number of documents to be returned by this operation.
     *
     * @param limit the maximum number of documents to return. If null, no limit is applied.
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * Retrieves the skip value for the current operation.
     * The skip value determines the number of documents to skip
     * in the result set during a MongoDB query.
     *
     * @return the number of documents to skip as an Integer, or null if not set.
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Sets the number of documents to skip in the operation.
     *
     * @param skip the number of documents to skip in the query results. A value of null indicates no skipping.
     */
    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    /**
     * Retrieves the flag indicating whether the record ID should be included in the results.
     *
     * @return a {@code Boolean} value representing whether the record ID is included
     *         in the query results. Returns {@code true} if the record ID is included,
     *         {@code false} otherwise.
     */
    public Boolean getIncludeRecordId() {
        return includeRecordId;
    }

    /**
     * Sets whether the record ID should be included in the query results.
     *
     * @param includeRecordId a Boolean specifying whether to include the record ID in the results.
     */
    public void setIncludeRecordId(Boolean includeRecordId) {
        this.includeRecordId = includeRecordId;
    }

    /**
     * Retrieves the maximum time, in milliseconds, for a given operation.
     *
     * <p>
     * This value is typically used to specify the maximum amount of time
     * the server will allow for an operation to execute before timing out.
     * </p>
     *
     * @return an {@link Integer} representing the maximum time in milliseconds, or null if not set
     */
    public Integer getMaxTime() {
        return maxTime;
    }

    /**
     * Sets the maximum amount of time, in milliseconds, for the operation to execute.
     *
     * @param maxTime the maximum time in milliseconds to allow the operation to run.
     *                A value of null indicates no time limit.
     */
    public void setMaxTime(Integer maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * Retrieves the collation configuration for the operation.
     *
     * @return the {@link Collation} object representing the collation settings
     *         used to control the comparison rules for string data during the operation.
     */
    public Collation getCollation() {
        return collation;
    }

    /**
     * Sets the collation used for operations that support locale-specific behavior,
     * such as string comparisons. Collation allows for customization of string
     * comparisons to match specific language and cultural rules.
     *
     * @param collation the {@link Collation} object defining the collation settings.
     *                  This includes options like locale, case sensitivity, and more.
     */
    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}