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
 * Bulk Write Operation -
 * Acts as a utility class for parsing the bulk write operations JSON Array string and creating a List of WriteModel for
 * execution of the operations in Mongo DB instance.
 * */
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
    private static final String SUPPORTED_OPS_STR = String.join(", ", SUPPORTED_OPS).trim();

    private String operationsJson;
    private Boolean skipDateTimeConversion;
    private Boolean isOrdered;
    private List<WriteModel<Document>> bulkWriteOperations;

    public BulkWriteOperation(String databaseName, Boolean validateDatabase, String collectionName, Boolean validateCollection, String operationsJson, Boolean isOrdered, Boolean skipDateTimeConversion) throws Exception{
        super(databaseName, validateDatabase, collectionName, validateCollection);

        //set the data
        setOperationsJson(operationsJson);
        setSkipDateTimeConversion(skipDateTimeConversion);
        setIsOrdered(isOrdered);

        //parse the operations
        setBulkWriteOperations(this.parseQuery(this.operationsJson));
    }

    //getters and setters
    //json
    public void setOperationsJson(String data) {
        this.operationsJson = data;
    }
    public String getOperationsJson() {
        return this.operationsJson;
    }

    //is ordered
    public void setIsOrdered(Boolean isOrdered) {
        this.isOrdered = isOrdered;
    }
    public boolean getIsOrdered() {
        return this.isOrdered != null && this.isOrdered;
    }

    //skip date time conversion
    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }
    public boolean getSkipDateTimeConversion() {
        return this.skipDateTimeConversion != null && this.skipDateTimeConversion;
    }

    //bulk write operations
    public void setBulkWriteOperations(List<WriteModel<Document>> bulkWriteOperations) {
        this.bulkWriteOperations = bulkWriteOperations;
    }
    public List<WriteModel<Document>> getBulkWriteOperations() {
        return this.bulkWriteOperations;
    }

    /**
     * getOperation - Retrieves the type of operation from the JSON Object's keys.
     * Throws an InvalidMongoOperationException if there is no operation specified or
     * the specified operation is not one of the following supported operations as listed in the Mongo DB
     * documentation - insertOne, deleteOne, deleteMany, replaceOne, updateOne and updateMany.
     * */
    public String getOperation(Set<String> keys) throws InvalidMongoOperationException {
        if (keys == null) throw new InvalidMongoOperationException("No operation was specified.");
        for (String key : keys) {
            if (SUPPORTED_OPS.contains(key)) return key;
        }
        throw new InvalidMongoOperationException();
    }

    /**
     * createCollation - Returns a Mongo Db Collation object from the provided
     * collation property JSON object.
     * */
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
     * getOrDefaultFilter - Returns the specified filter document or a default empty filter document
     * based on the provided operation JSON object.
     * Returns an empty Bson Document if there is no 'filter' property in the provided operation or the filter property is empty or '{}'.
     * Otherwise, parses the provided filter to a Bson document and returns it.
     * */
    private Document getOrDefaultFilter (JSONObject documentJSON) {
        if (!documentJSON.has(FILTER)) return new Document();
        if (documentJSON.get(FILTER) == null) return new Document();
        if (documentJSON.get(FILTER) instanceof JSONArray && documentJSON.getJSONArray(FILTER).isEmpty()) return new Document();
        if (StringUtils.isEmpty(documentJSON.get(FILTER).toString())) return new Document();
        return Document.parse(documentJSON.getJSONObject(FILTER).toString());
    }

    /**
     * createOperation - Gets the JSON Object and parses it into WriteModel instances based
     * on the specified operation.
     * 1. insertOne - Keys: document (required) - returns InsertOneModel
     * 2. deleteOne - Keys: filter, collation - returns DeleteOneModel
     * 3. deleteMany - Keys: filter, collation - returns DeleteManyModel
     * 4. replaceOne - Keys: replacement (required), filter, upsert (defaults to false), collation - returns ReplaceOneModel
     * 5. updateOne - Keys: update (required), filter, upsert (defaults to false), collation, arrayFilters, let - returns UpdateOneModel
     * 6. updateMany - Keys: update (required), filter, upsert (defaults to false), collation, arrayFilters, let - returns UpdateManyModel
     * Throws InvalidJsonException and InvalidMongoOperationException.
     * */
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
                    return new InsertOneModel<Document>(document);
                }
                case DELETE_ONE:
                case DELETE_MANY: {
                    //keys - filter, collation
                    DeleteOptions deleteOptions = new DeleteOptions();
                    if (collation != null) deleteOptions = deleteOptions.collation(createCollation(operationJSON.getJSONObject(COLLATION)));
                    if (operation.equals(DELETE_MANY)) return new DeleteManyModel<Document>(filter, deleteOptions);
                    else return new DeleteOneModel<Document>(filter, deleteOptions);
                }
                case REPLACE_ONE: {
                    //keys - replacement (r), filter, upsert (def - false), collation
                    Document replacement = Document.parse(operationJSON.getJSONObject(REPLACEMENT).toString());
                    ReplaceOptions replaceOptions = new ReplaceOptions();
                    if (collation != null) replaceOptions = replaceOptions.collation(collation);
                    if (operationJSON.has("upsert")) replaceOptions = replaceOptions.upsert(operationJSON.getBoolean("upsert"));
                    return new ReplaceOneModel<Document>(MongoDocumentUtil.prepDocumentForInsert(filter, this.skipDateTimeConversion), MongoDocumentUtil.prepDocumentForInsert(replacement, this.skipDateTimeConversion), replaceOptions);
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
                    if (operation.equals(UPDATE_ONE)) return new UpdateOneModel<Document>(MongoDocumentUtil.prepDocumentForInsert(filter, this.skipDateTimeConversion), MongoDocumentUtil.prepDocumentForInsert(update, this.skipDateTimeConversion), updateOptions);
                    else return new UpdateManyModel<Document>(filter, update, updateOptions);
                }
            }
        } catch (Exception e) {
            throw new InvalidJsonException("The operation "+operation+" has an invalid structure.", operationJSON.toString());
        }
        return null;
    }

    /**
     * parseQuery - Parses the JSON array string provided and returns
     * a list of WriteModel instances.
     * Throws InvalidJsonException, InvalidMongoOperationException.
     * */
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
