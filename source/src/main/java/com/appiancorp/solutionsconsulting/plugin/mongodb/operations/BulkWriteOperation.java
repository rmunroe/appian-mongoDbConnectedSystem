package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidMongoOperationException;
import com.mongodb.client.model.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The BulkWriteOperation class extends the CollectionWriteOperation
 * class and provides functionality for performing bulk write operations
 * on a MongoDB collection. It supports multiple types of write operations
 * such as insert, update, delete, and replace. Operations are parsed and
 * executed based on the provided JSON representation.
 *
 * <p>
 * The class includes methods to configure operation settings, handle JSON
 * parsing, and create corresponding WriteModel instances. Supported
 * operations include:
 * - insertOne
 * - deleteOne
 * - deleteMany
 * - replaceOne
 * - updateOne
 * - updateMany
 * </p>
 *
 * <p>
 * This class ensures support for optional features such as collation,
 * upserts, array filters, and more as required by MongoDB bulk write
 * specifications.
 * </p>
 *
 * @author Vuram SWAT
 * @since 1.4
 */
public class BulkWriteOperation extends CollectionWriteOperation{
    private static final String FILTER = "filter";
    private static final String INSERT_ONE = "insertOne";
    private static final String UPDATE_ONE = "updateOne";
    private static final String UPDATE_MANY = "updateMany";
    private static final String DELETE_ONE = "deleteOne";
    private static final String DELETE_MANY = "deleteMany";
    private static final String REPLACE_ONE = "replaceOne";
    private static final String REPLACEMENT = "replacement";
    private static final String DOCUMENT = "document";
    private static final String COLLATION = "collation";
    private static final List<String> SUPPORTED_OPS = Arrays.asList(INSERT_ONE, UPDATE_ONE, UPDATE_MANY, REPLACE_ONE, DELETE_MANY, DELETE_ONE);

    private String operationsJson;
    private Boolean skipDateTimeConversion;
    private Boolean isOrdered;
    private List<WriteModel<Document>> bulkWriteOperations;

    /**
     * Constructs a {@code BulkWriteOperation} with the specified database and collection context
     * along with bulk write operation configurations and execution parameters.
     *
     * @param databaseName         the name of the database to target
     * @param validateDatabase     whether the database should be validated prior to running the operation
     * @param collectionName       the name of the collection to target
     * @param validateCollection   whether the collection should be validated prior to running the operation
     * @param operationsJson       a JSON string representing an array of operations to be performed
     * @param isOrdered            specifies whether the operations should be executed in an ordered sequence
     * @param skipDateTimeConversion whether the conversion of date-time fields should be skipped in the operations
     * @throws Exception           if an error occurs during initialization or parsing of operations
     */
    public BulkWriteOperation(String databaseName, Boolean validateDatabase, String collectionName, Boolean validateCollection, String operationsJson, Boolean isOrdered, Boolean skipDateTimeConversion) throws Exception{
        super(databaseName, validateDatabase, collectionName, validateCollection);

        //set the data
        setOperationsJson(operationsJson);
        setSkipDateTimeConversion(skipDateTimeConversion);
        setIsOrdered(isOrdered);

        //parse the operations
        setBulkWriteOperations(this.parseQuery(this.operationsJson));
    }


    /**
     * Sets the JSON representation of the bulk write operations.
     *
     * @param data a JSON string that represents an array of operations to be performed.
     *             The operations may include actions such as insert, update, delete, or replace
     *             and should follow the expected structure for MongoDB bulk write operations.
     */
    public void setOperationsJson(String data) {
        this.operationsJson = data;
    }

    /**
     * Retrieves the JSON string representation of the bulk write operations.
     *
     * @return a JSON string representing an array of operations to be performed, such as insert, update, delete, or replace.
     */
    public String getOperationsJson() {
        return this.operationsJson;
    }

    /**
     * Sets whether the bulk write operations should be executed in an ordered sequence.
     *
     * @param isOrdered a Boolean value where true indicates that the operations are executed
     *                  in sequential order and false indicates unordered execution.
     */
    public void setIsOrdered(Boolean isOrdered) {
        this.isOrdered = isOrdered;
    }

    /**
     * Determines whether the bulk write operations are ordered.
     *
     * @return {@code true} if the bulk write operations are executed in order;
     *         {@code false} if they are executed out of order or if the ordering is unspecified.
     */
    public boolean getIsOrdered() {
        return this.isOrdered != null && this.isOrdered;
    }

    /**
     * Sets whether the conversion of date-time fields should be skipped during the bulk write operations.
     *
     * @param skipDateTimeConversion a Boolean value where true indicates that date-time conversion
     *                               should be skipped, and false indicates that date-time conversion should occur*/
    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }

    /**
     * Determines whether date-time conversions should be skipped.
     *
     * @return {@code true} if date-time field conversion is to be skipped; {@code false} otherwise.
     */
    public boolean getSkipDateTimeConversion() {
        return this.skipDateTimeConversion != null && this.skipDateTimeConversion;
    }

    /**
     * Sets the bulk write operations to be performed.
     *
     * @param bulkWriteOperations a list of {@link WriteModel} objects representing the bulk
     *                            write operations to be executed. Each {@code WriteModel} can represent
     *                            an insert, update, delete, or replace operation for MongoDB documents*/
    public void setBulkWriteOperations(List<WriteModel<Document>> bulkWriteOperations) {
        this.bulkWriteOperations = bulkWriteOperations;
    }

    /**
     * Retrieves the list of bulk write operations to be executed.
     *
     * @return a list of {@code WriteModel<Document>} objects representing bulk write operations
     *         such as insert, update, delete, or replace to be performed on a MongoDB collection.
     */
    public List<WriteModel<Document>> getBulkWriteOperations() {
        return this.bulkWriteOperations;
    }

    /**
     * Determines and retrieves the operation specified in the provided set of keys.
     * The method checks whether the given keys match any supported operations.
     * If a supported operation is found, it is returned. If no supported operations
     * are found or if the keys set is null, the method throws an exception.
     *
     * @param keys a set of strings representing the operation keys to evaluate
     * @return the supported operation key as a string if a match is found
     * @throws InvalidMongoOperationException if the keys set is null or no supported operation is found
     */
    public String getOperation(Set<String> keys) throws InvalidMongoOperationException {
        if (keys == null) throw new InvalidMongoOperationException("No operation was specified.");
        for (String key : keys) {
            if (SUPPORTED_OPS.contains(key)) return key;
        }
        throw new InvalidMongoOperationException();
    }

    /**
     * Creates a {@link Collation} object using the properties defined in the given JSON object.
     * The method extracts various collation-related parameters such as locale, case sensitivity,
     * strength, numeric ordering, and other attributes, and constructs a {@code Collation} instance accordingly.
     *
     * @param collationJSON a {@link JSONObject} containing the collation settings. It should include properties
     *                      such as "locale", "caseLevel", "caseFirst", "strength", "numericOrdering",
     *                      "alternate", "maxVariable", and "backwards". If {@code collationJSON} is null
     *                      or empty, the method returns null.
     *
     * @return a {@link Collation} object built from the provided JSON properties, or null if
     *         {@code collationJSON} is null or empty.
     */
    private Collation createCollation(JSONObject collationJSON) {
        if (collationJSON == null || StringUtils.isEmpty(collationJSON.toString())) return null;
        Collation.Builder collationBuilder = Collation.builder();
        collationBuilder.locale(collationJSON.getString("locale"));
        if (collationJSON.has("caseLevel")) collationBuilder.caseLevel(collationJSON.getBoolean("caseLevel"));
        if (collationJSON.has("caseFirst")) collationBuilder.collationCaseFirst(CollationCaseFirst.fromString(collationJSON.getString("caseFirst")));
        if (collationJSON.has("strength")) collationBuilder.collationStrength(CollationStrength.fromInt(collationJSON.getInt("strength")));
        if (collationJSON.has("numericOrdering")) collationBuilder.numericOrdering(collationJSON.getBoolean("numericOrdering"));
        if (collationJSON.has("alternate")) collationBuilder.collationAlternate(CollationAlternate.fromString(collationJSON.getString("alternate")));
        if (collationJSON.has("maxVariable")) collationBuilder.collationMaxVariable(CollationMaxVariable.fromString(collationJSON.getString("maxVariable")));
        if (collationJSON.has("backwards")) collationBuilder.backwards(collationJSON.getBoolean("backwards"));

        return collationBuilder.build();
    }

    /**
     * Retrieves a filter from the provided JSON object or returns a default empty Document
     * object if the filter is missing, null, or invalid.
     *
     * <p>
     * The method checks if the JSON object contains a valid filter. If the filter is missing,
     * empty, or invalid, it returns a new, empty Document. Otherwise, it parses the filter
     * from the JSON object and returns it as a Document.
     * </p>
     *
     * @param documentJSON the JSON object containing the filter data. It is expected to contain
     *                     a valid filter under the FILTER key.
     * @return a {@link Document} object representing the filter if valid, or a new empty
     *         {@link Document} object if the filter is invalid or missing.
     */
    private Document getOrDefaultFilter (JSONObject documentJSON) {
        if (!documentJSON.has(FILTER)) return new Document();
        if (documentJSON.get(FILTER) == null) return new Document();
        if (documentJSON.get(FILTER) instanceof JSONArray && documentJSON.getJSONArray(FILTER).isEmpty()) return new Document();
        if (StringUtils.isEmpty(documentJSON.get(FILTER).toString())) return new Document();
        return Document.parse(documentJSON.getJSONObject(FILTER).toString());
    }


    /**
     * Creates a MongoDB write operation model based on the provided JSON object.
     *
     * <p>
     *   <ol>
     *     <li>insertOne - Keys: document (required) - returns InsertOneModel</li>
     *     <li>deleteOne - Keys: filter, collation - returns DeleteOneModel</li>
     *     <li>deleteMany - Keys: filter, collation - returns DeleteManyModel</li>
     *     <li>replaceOne - Keys: replacement (required), filter, upsert (defaults to false), collation - returns ReplaceOneModel</li>
     *     <li>updateOne - Keys: update (required), filter, upsert (defaults to false), collation, arrayFilters, let - returns UpdateOneModel</li>
     *     <li>updateMany - Keys: update (required), filter, upsert (defaults to false), collation, arrayFilters, let - returns UpdateManyModel</li>
     *   </ol>
     * </p>
     *
     * @param operationJSON A JSON object describing the operation to be performed. It must have a recognized
     *                      operation key (e.g., "insertOne", "deleteOne", "deleteMany", "replaceOne", "updateOne",
     *                      or "updateMany") and the corresponding fields required for the specified operation.
     *                      Additional supported options may include collation, upsert, arrayFilters, and let.
     * @return A {@link WriteModel} representing the specified MongoDB operation. This can be an instance of
     *         {@link InsertOneModel}, {@link DeleteOneModel}, {@link DeleteManyModel}, {@link ReplaceOneModel},
     *         {@link UpdateOneModel}, or {@link UpdateManyModel}, depending on the operation type provided.
     * @throws InvalidJsonException If the provided JSON object has an invalid structure that doesn't adhere to
     *                               the expected format for the specified operation type.
     * @throws InvalidMongoOperationException If an invalid operation type is provided or if there's an issue
     *                                         during the creation of the operation model.
     */
    private WriteModel<Document> createOperation(JSONObject operationJSON) throws InvalidJsonException, InvalidMongoOperationException {
        String operation = this.getOperation(operationJSON.keySet());
        //validate the operation key
        try {
            operationJSON = operationJSON.getJSONObject(operation);
            Document filter = getOrDefaultFilter(operationJSON);
            Collation collation = (operationJSON.has(COLLATION))? this.createCollation(operationJSON.getJSONObject(COLLATION)): null;
            switch (operation) {
                case INSERT_ONE: {
                    //keys - document (r)
                    Document document = Document.parse(operationJSON.getJSONObject(DOCUMENT).toString());
                    MongoDocumentUtil.prepDocumentForInsert(document, this.skipDateTimeConversion);
                    return new InsertOneModel<>(document);
                }
                case DELETE_ONE:
                case DELETE_MANY: {
                    //keys - filter, collation
                    DeleteOptions deleteOptions = new DeleteOptions();
                    if (collation != null) deleteOptions = deleteOptions.collation(createCollation(operationJSON.getJSONObject(COLLATION)));
                    if (operation.equals(DELETE_MANY)) return new DeleteManyModel<>(filter, deleteOptions);
                    else return new DeleteOneModel<>(filter, deleteOptions);
                }
                case REPLACE_ONE: {
                    //keys - replacement (r), filter, upsert (def - false), collation
                    Document replacement = Document.parse(operationJSON.getJSONObject(REPLACEMENT).toString());
                    ReplaceOptions replaceOptions = new ReplaceOptions();
                    if (collation != null) replaceOptions = replaceOptions.collation(collation);
                    if (operationJSON.has("upsert")) replaceOptions = replaceOptions.upsert(operationJSON.getBoolean("upsert"));
                    return new ReplaceOneModel<>(MongoDocumentUtil.prepDocumentForInsert(filter, this.skipDateTimeConversion), MongoDocumentUtil.prepDocumentForInsert(replacement, this.skipDateTimeConversion), replaceOptions);
                }
                case UPDATE_ONE:
                case UPDATE_MANY: {
                    //keys - filter, update (r), upsert (def - false), collation, arrayFilters, let
                    Document update = Document.parse(operationJSON.getJSONObject("update").toString());
                    UpdateOptions updateOptions = new UpdateOptions();
                    if (collation != null) updateOptions = updateOptions.collation(collation);
                    if (operationJSON.has("upsert")) updateOptions = updateOptions.upsert(operationJSON.getBoolean("upsert"));
                    if (operationJSON.has("arrayFilters") && operationJSON.get("arrayFilters") != null) {
                        JSONArray arrayFilters = operationJSON.getJSONArray("arrayFilters");
                        List<Document> filters = new ArrayList<>();
                        for (int index = 0; index < arrayFilters.length(); index++) {
                            if (StringUtils.isNotEmpty(arrayFilters.getJSONObject(index).toString())) {
                                filters.add(Document.parse(arrayFilters.getJSONObject(index).toString()));
                            }
                        }
                        updateOptions = updateOptions.arrayFilters(filters);
                    }
                    if (operation.equals(UPDATE_ONE)) return new UpdateOneModel<>(MongoDocumentUtil.prepDocumentForInsert(filter, this.skipDateTimeConversion), MongoDocumentUtil.prepDocumentForInsert(update, this.skipDateTimeConversion), updateOptions);
                    else return new UpdateManyModel<>(filter, update, updateOptions);
                }
            }
        } catch (Exception e) {
            throw new InvalidJsonException("The operation "+operation+" has an invalid structure.", operationJSON.toString());
        }
        return null;
    }

    /**
     * Parses a JSON string representation of an array of MongoDB operations
     * and converts it into a list of {@link WriteModel} objects to be executed as part of a bulk write operation.
     *
     * @param queryStr a JSON string representing an array of operations. Each operation should follow
     *                 MongoDB's bulk write syntax, such as insert, update, delete, or replace.
     * @return a list of {@link WriteModel<Document>} objects representing the parsed operations.
     * @throws InvalidJsonException if the given JSON string is invalid or cannot be parsed properly.
     * @throws InvalidMongoOperationException if an invalid MongoDB operation is encountered.
     */
    private List<WriteModel<Document>> parseQuery(String queryStr) throws InvalidJsonException, InvalidMongoOperationException {
        List<WriteModel<Document>> operations = new ArrayList<>();
        //holders
        try {
            JSONArray operationsJSON = new JSONArray(queryStr);
            JSONObject operationJSON;

            for (int index = 0; index < operationsJSON.length(); index++) {
                operationJSON = operationsJSON.getJSONObject(index);
                if (StringUtils.isNotEmpty(operationJSON.toString())) {
                    //create the operation
                    operations.add(this.createOperation(operationJSON));
                }
            }
        } catch (InvalidMongoOperationException | InvalidJsonException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidJsonException("The list of operations provided is invalid.", queryStr);
        }

        return operations;
    }
}
