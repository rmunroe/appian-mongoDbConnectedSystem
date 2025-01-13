package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.Map;


/**
 * Represents a MongoDB count operation on a collection with support for filters
 * and collation settings. This class extends {@link CollectionReadOperation} and is
 * used to count the number of documents in a collection based on specified query
 * filters and collation options.
 *
 * <p>
 * The class provides methods for setting and retrieving the query filter
 * (specified as a JSON string or {@link Document}) and the collation settings,
 * which define locale-specific string comparison rules during the operation.
 * </p>
 *
 * <p>
 * It validates the provided JSON filter string and converts it into a
 * {@link Document} format. Additionally, it includes diagnostic information
 * about the filter and collation as part of the operation.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class CollectionCountOperation extends CollectionReadOperation {
    private String filterJson;
    private Document filterDocument;
    private Collation collation;


    /**
     * Constructs a new instance of the {@code CollectionCountOperation} class,
     * representing a MongoDB count operation that counts documents in a collection
     * based on specified filters and collation settings.
     *
     * @param databaseName the name of the database where the collection resides
     * @param validateDatabase whether to validate the existence of the database
     * @param collectionName the name of the collection to perform the count operation on
     * @param validateCollection whether to validate the existence of the collection
     * @param readPreference the read preference settings for the operation
     * @param readConcern the read concern settings for the operation
     * @param filterJson a JSON string representing the query filter for the count operation
     * @param collation the collation settings for the operation, determining
     *        locale-specific string comparison rules
     * @throws InvalidJsonException if the provided filterJson is not a valid JSON string
     */
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
                        "Filter JSON Query: Invalid JSON provided.",
                        getFilterJson());
            }

        setCollation(collation);
    }

    /**
     * Retrieves diagnostic information related to the count operation,
     * including additional details specific to query filter and collation settings.
     * This method extends the diagnostics provided by the parent class by
     * including entries for "Query Filter JSON" and "Collation".
     *
     * <p>
     * "Query Filter JSON" provides the raw JSON representation of the query filter
     * used in the operation, and "Collation" includes the collation settings
     * as a JSON string, if any are specified.
     * </p>
     *
     * @return a {@link Map} containing diagnostic key-value pairs, where the keys are
     *         strings such as "Query Filter JSON" and "Collation" and the values contain
     *         their respective data or null if unavailable.
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Query Filter JSON", getFilterJson());
        diagnostic.put("Collation", (getCollation() == null) ? null : getCollation().asDocument().toJson());

        return diagnostic;
    }

    /**
     * Retrieves the JSON string that represents the filter used in the MongoDB operation.
     *
     * @return a String containing the JSON representation of the filter query
     */
    public String getFilterJson() {
        return filterJson;
    }

    /**
     * Sets the filter JSON string to be used for the MongoDB operation.
     * The filter JSON represents the query filter criteria in a JSON format.
     *
     * @param filterJson the filter JSON string to be set
     */
    public void setFilterJson(String filterJson) {
        this.filterJson = filterJson;
    }

    /**
     * Retrieves the parsed filter document used to define the query criteria
     * for the count operation. The filter document is represented as a BSON
     * {@link Document}, which can be used to specify conditions for matching
     * documents in the MongoDB collection.
     *
     * @return the {@link Document} representing the filter criteria,
     *         or null if no filter has been set or parsed.
     */
    public Document getFilterDocument() {
        return filterDocument;
    }

    /**
     * Sets the filter document used for the operation.
     *
     * @param filterDocument the {@link Document} instance representing the query
     *                        filter to be applied in the operation.
     */
    public void setFilterDocument(Document filterDocument) {
        this.filterDocument = filterDocument;
    }

    /**
     * Retrieves the collation settings for the operation.
     *
     * @return the {@link Collation} object representing locale-specific string
     *         comparison rules, or null if no collation is set.
     */
    public Collation getCollation() {
        return collation;
    }

    /**
     * Sets the collation to be used for the operation.
     * The collation specifies locale-specific rules for string comparison,
     * such as case sensitivity or accent marks, that are applied during the MongoDB operation.
     *
     * @param collation The {@link Collation} object containing the locale-specific
     *                  string comparison rules to apply.
     */
    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}