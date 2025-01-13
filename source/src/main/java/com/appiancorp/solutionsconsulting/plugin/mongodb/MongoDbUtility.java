package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.*;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A utility class for executing MongoDB operations within the Appian Connected System.
 * <p>
 * This class retrieves a {@link MongoClient} via {@link MongoDbConnection} using the
 * configured connection string, and then provides methods for:
 * <ul>
 *   <li>Listing databases and collections</li>
 *   <li>Finding and aggregating documents</li>
 *   <li>Inserting, updating, and deleting documents</li>
 *   <li>Managing collections (create, drop) and indexes</li>
 *   <li>Performing bulk write operations</li>
 * </ul>
 * Methods often return data as either a {@code List<Map<String, Object>>}
 * (useful for Appian) or as raw JSON strings.
 * <p>
 * If database or collection validation is requested and the specified resource
 * does not exist, custom exceptions such as {@link MissingDatabaseException}
 * or {@link MissingCollectionException} are thrown.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Build or retrieve a SimpleConfiguration that includes the connection string
 * SimpleConfiguration config = ...;
 *
 * // Initialize the utility
 * MongoDbUtility mongoUtil = new MongoDbUtility(config);
 *
 * // List all databases
 * List<Map<String, Object>> databases = mongoUtil.listDatabases();
 *
 * // Perform a find operation
 * CollectionFindOperation op = new CollectionFindOperation("myDatabase", "myCollection", true, true);
 * List<Map<String, Object>> docs = mongoUtil.find(op);
 * }</pre>
 *
 * <p><b>Note:</b> This class relies on other custom operation objects (e.g.
 * {@link CollectionFindOperation}, {@link UpdateOperation}), each containing the
 * necessary parameters for the operation at hand.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class MongoDbUtility {

    /**
     * The underlying MongoDB client, retrieved from {@link MongoDbConnection}.
     */
    private final MongoClient mongoClient;

    /**
     * Constructs a new {@code MongoDbUtility} using the specified
     * {@link SimpleConfiguration}, which contains the MongoDB connection string.
     *
     * @param connectedSystemConfiguration the configuration from which to retrieve
     *                                     the connection string
     */
    public MongoDbUtility(SimpleConfiguration connectedSystemConfiguration) {
        this.mongoClient = MongoDbConnection.get(
                connectedSystemConfiguration.getValue(MongoDbConnectedSystemTemplate.CONNECTION_STRING)
        );
    }

    /**
     * Retrieves the specified database, optionally validating that it exists in the cluster.
     *
     * @param databaseName the name of the database to retrieve
     * @param validate     if true, verifies that the database exists; otherwise, no check is performed
     * @return a {@link MongoDatabase} instance
     * @throws MissingDatabaseException if {@code validate} is true and the database does not exist
     */
    private MongoDatabase getDatabase(String databaseName, Boolean validate) throws MissingDatabaseException {
        if (validate != null && validate) {
            List<String> databaseNames = new ArrayList<>();
            this.mongoClient.listDatabaseNames().into(databaseNames);
            if (!databaseNames.contains(databaseName)) {
                throw new MissingDatabaseException(databaseName);
            }
        }
        return this.mongoClient.getDatabase(databaseName);
    }

    /**
     * Retrieves the specified collection, optionally validating that it exists in the database.
     *
     * @param database       a {@link MongoDatabase} reference
     * @param collectionName the name of the collection to retrieve
     * @param validate       if true, verifies that the collection exists; otherwise, no check is performed
     * @return a {@link MongoCollection} of {@link Document}
     * @throws MissingCollectionException if {@code validate} is true and the collection does not exist
     */
    private MongoCollection<Document> getCollection(MongoDatabase database, String collectionName, Boolean validate)
            throws MissingCollectionException {
        if (validate != null && validate) {
            List<String> collectionNames = new ArrayList<>();
            database.listCollectionNames().into(collectionNames);
            if (!collectionNames.contains(collectionName)) {
                throw new MissingCollectionException(collectionName);
            }
        }
        return database.getCollection(collectionName);
    }

    /**
     * Lists all databases as a list of {@code Map<String, Object>} for consumption in Appian.
     *
     * @return a list of maps, each representing metadata about a database
     */
    public List<Map<String, Object>> listDatabases() {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Document doc : this.mongoClient.listDatabases()) {
            results.add(doc);
        }
        return results;
    }

    /**
     * Similar to {@link #listDatabases()} but returns each database as a JSON string.
     *
     * @return a list of JSON-formatted strings representing each database
     */
    public List<String> listDatabasesJson() {
        List<String> results = new ArrayList<>();
        for (Document doc : this.mongoClient.listDatabases()) {
            results.add(doc.toJson());
        }
        return results;
    }

    /**
     * Lists all collections within a specified database as a list of {@code Map<String, Object>}.
     * Optionally validates that the database exists, and can convert any UUID fields to strings if requested.
     *
     * @param databaseName     the name of the database to query
     * @param validateDatabase if true, validates that the database exists
     * @param uuidAsString     if true, UUID fields are converted to string format
     * @return a list of maps, each representing a collection
     * @throws MissingDatabaseException if {@code validateDatabase} is true and the database doesn't exist
     */
    public List<Map<String, Object>> listCollections(String databaseName, Boolean validateDatabase, Boolean uuidAsString)
            throws MissingDatabaseException {
        MongoDatabase database = getDatabase(databaseName, validateDatabase);
        List<Map<String, Object>> results = new ArrayList<>();
        for (Document doc : database.listCollections()) {
            results.add(MongoDocumentUtil.prepDocumentForOutput(doc, false, uuidAsString));
        }
        return results;
    }

    /**
     * Similar to {@link #listCollections(String, Boolean, Boolean)} but returns
     * each collection's information as a JSON string.
     *
     * @param databaseName     the name of the database to query
     * @param validateDatabase if true, validates that the database exists
     * @return a list of JSON-formatted strings representing each collection
     * @throws MissingDatabaseException if {@code validateDatabase} is true and the database doesn't exist
     */
    public List<String> listCollectionsJson(String databaseName, Boolean validateDatabase) throws MissingDatabaseException {
        MongoDatabase database = getDatabase(databaseName, validateDatabase);
        List<String> results = new ArrayList<>();
        for (Document doc : database.listCollections()) {
            results.add(doc.toJson());
        }
        return results;
    }

    /**
     * Finds documents in a collection according to the specified {@link CollectionFindOperation} parameters,
     * returning them as a list of maps.
     *
     * @param op a {@link CollectionFindOperation} describing filter, sort, projection, etc.
     * @return a list of maps, each representing a found document
     * @throws MissingDatabaseException    if {@code op.getValidateDatabase()} is true and the database doesn't exist
     * @throws MissingCollectionException  if {@code op.getValidateCollection()} is true and the collection doesn't exist
     */
    public List<Map<String, Object>> find(CollectionFindOperation op)
            throws MissingDatabaseException, MissingCollectionException {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Document doc : execFind(op)) {
            results.add(MongoDocumentUtil.prepDocumentForOutput(doc, true, false));
        }
        return results;
    }

    /**
     * Finds documents in a collection according to the specified {@link CollectionFindOperation} parameters,
     * returning them as JSON strings.
     *
     * @param op a {@link CollectionFindOperation} describing filter, sort, projection, etc.
     * @return a list of JSON-formatted strings, each representing a found document
     * @throws MissingDatabaseException    if the database doesn't exist and validation is required
     * @throws MissingCollectionException  if the collection doesn't exist and validation is required
     */
    public List<String> findJson(CollectionFindOperation op)
            throws MissingDatabaseException, MissingCollectionException {
        List<String> results = new ArrayList<>();
        for (Document doc : execFind(op)) {
            results.add(doc.toJson());
        }
        return results;
    }

    /**
     * Executes a find operation using the parameters from the given {@link CollectionFindOperation}.
     * Applies optional read preference, read concern, collation, limit, skip, etc.
     *
     * @param op the {@link CollectionFindOperation} describing query details
     * @return a {@link FindIterable} of {@link Document}
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    private FindIterable<Document> execFind(CollectionFindOperation op)
            throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        if (StringUtils.isNotEmpty(op.getReadPreference())) {
            collection = collection.withReadPreference(Objects.requireNonNull(getReadPreference(op.getReadPreference())));
        }
        if (StringUtils.isNotEmpty(op.getReadConcern())) {
            collection = collection.withReadConcern(getReadConcern(op.getReadConcern()));
        }

        Document filterDoc = op.getFilterDocument();
        FindIterable<Document> results = (filterDoc == null) ? collection.find() : collection.find(filterDoc);

        if (op.getSortDocument() != null) results.sort(op.getSortDocument());
        if (op.getProjectionDocument() != null) results.projection(op.getProjectionDocument());
        if (op.getCollation() != null) results.collation(op.getCollation());
        if (op.getLimit() != null) results.limit(op.getLimit());
        if (op.getSkip() != null) results.skip(op.getSkip());
        if (op.getIncludeRecordId() != null && op.getIncludeRecordId()) results.showRecordId(true);
        if (op.getMaxTime() != null) results.maxTime(op.getMaxTime(), TimeUnit.MILLISECONDS);

        return results;
    }

    /**
     * Counts the documents in the specified collection according to the given {@link CollectionCountOperation}.
     *
     * @param op a {@link CollectionCountOperation} describing the database, collection, and filter document
     * @return the number of documents matching the filter
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public long count(CollectionCountOperation op)
            throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        if (StringUtils.isNotEmpty(op.getReadPreference())) {
            collection = collection.withReadPreference(Objects.requireNonNull(getReadPreference(op.getReadPreference())));
        }
        if (StringUtils.isNotEmpty(op.getReadConcern())) {
            collection = collection.withReadConcern(getReadConcern(op.getReadConcern()));
        }

        return collection.countDocuments(op.getFilterDocument());
    }

    /**
     * Performs an aggregation using the specified {@link CollectionAggregateOperation} and returns
     * the results as a list of maps (key-value pairs).
     *
     * @param op a {@link CollectionAggregateOperation} describing the pipeline stages
     * @return a list of maps, each representing an aggregated document
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public List<Map<String, Object>> aggregate(CollectionAggregateOperation op)
            throws MissingDatabaseException, MissingCollectionException {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Document doc : aggregateExec(op)) {
            results.add(MongoDocumentUtil.prepDocumentForOutput(doc, true, false));
        }
        return results;
    }

    /**
     * Performs an aggregation using the specified {@link CollectionAggregateOperation} and returns
     * the results as JSON strings.
     *
     * @param op a {@link CollectionAggregateOperation} describing the pipeline stages
     * @return a list of JSON-formatted strings, each representing an aggregated document
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public List<String> aggregateJson(CollectionAggregateOperation op)
            throws MissingDatabaseException, MissingCollectionException {
        List<String> results = new ArrayList<>();
        for (Document doc : aggregateExec(op)) {
            results.add(doc.toJson());
        }
        return results;
    }

    /**
     * Executes the aggregation pipeline described by the {@link CollectionAggregateOperation}.
     * Applies optional read preference, read concern, and collation if specified.
     *
     * @param op a {@link CollectionAggregateOperation} containing the pipeline configuration
     * @return an {@link AggregateIterable} of {@link Document}
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public AggregateIterable<Document> aggregateExec(CollectionAggregateOperation op)
            throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        if (StringUtils.isNotEmpty(op.getReadPreference())) {
            collection = collection.withReadPreference(Objects.requireNonNull(getReadPreference(op.getReadPreference())));
        }
        if (StringUtils.isNotEmpty(op.getReadConcern())) {
            collection = collection.withReadConcern(getReadConcern(op.getReadConcern()));
        }

        AggregateIterable<Document> results = collection.aggregate(op.getStagesDocuments());
        if (op.getCollation() != null) results.collation(op.getCollation());

        return results;
    }

    /**
     * Inserts multiple documents into the specified collection.
     *
     * @param op an {@link InsertManyOperation} containing the database, collection,
     *           and list of documents to insert
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public void insertMany(InsertManyOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());
        collection.insertMany(op.getDocuments());
    }

    /**
     * Inserts a single document into the specified collection and returns a prepped
     * version of that document (with any needed transformations for Appian).
     *
     * @param op an {@link InsertOneOperation} containing the database, collection,
     *           and the document to insert
     * @return the inserted document, prepped via {@link MongoDocumentUtil}
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public Document insertOne(InsertOneOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        Document document = op.getDocument();
        collection.insertOne(document);
        return MongoDocumentUtil.prepDocumentForOutput(document, true, true);
    }

    /**
     * Updates a single document in the specified collection, returning an object
     * detailing how many documents were matched, modified, or upserted.
     *
     * @param op an {@link UpdateOperation} describing the filter and update documents
     * @return a {@link Document} containing matchedCount, modifiedCount, upsertedId
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public Document updateOne(UpdateOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());
        return getDocumentFromUpdateResult(collection.updateOne(op.getFilterDocument(), op.getUpdateDocument()));
    }

    /**
     * Updates multiple documents in the specified collection, returning an object
     * detailing how many documents were matched, modified, or upserted.
     *
     * @param op an {@link UpdateOperation} describing the filter and update documents
     * @return a {@link Document} containing matchedCount, modifiedCount, upsertedId
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public Document updateMany(UpdateOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());
        return getDocumentFromUpdateResult(collection.updateMany(op.getFilterDocument(), op.getUpdateDocument()));
    }

    /**
     * Replaces one matching document in the specified collection with a new document,
     * returning an object detailing how many documents were matched, modified, or upserted.
     *
     * @param op a {@link ReplaceOneOperation} describing the filter and replacement document
     * @return a {@link Document} containing matchedCount, modifiedCount, upsertedId
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public Document replaceOne(ReplaceOneOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());
        return getDocumentFromUpdateResult(collection.replaceOne(op.getFilterDocument(), op.getReplacementDocument()));
    }

    /**
     * Deletes a single matching document from the specified collection, returning a
     * {@link Document} describing how many documents were deleted.
     *
     * @param op a {@link DeleteOperation} describing the filter document
     * @return a {@link Document} containing acknowledged and deletedCount
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public Document deleteOne(DeleteOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        DeleteOptions options = new DeleteOptions();
        if (op.getCollation() != null) {
            options.collation(op.getCollation());
        }
        return getDocumentFromDeleteResult(collection.deleteOne(op.getFilterDocument(), options));
    }

    /**
     * Deletes all matching documents from the specified collection, returning a
     * {@link Document} describing how many documents were deleted.
     *
     * @param op a {@link DeleteOperation} describing the filter document
     * @return a {@link Document} containing acknowledged and deletedCount
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public Document deleteMany(DeleteOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        DeleteOptions options = new DeleteOptions();
        if (op.getCollation() != null) {
            options.collation(op.getCollation());
        }
        return getDocumentFromDeleteResult(collection.deleteMany(op.getFilterDocument()));
    }

    /**
     * Executes a bulk write operation (consisting of multiple insert, update, or delete actions)
     * on the specified collection. Uses the {@link BulkWriteOperation} object to determine
     * whether the operations are ordered and what individual operations to perform.
     *
     * @param op a {@link BulkWriteOperation} containing the database, collection, and list of operations
     * @return a {@link BulkWriteResult} summarizing the execution outcome
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public BulkWriteResult bulkWrite(BulkWriteOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        List<WriteModel<Document>> operations = op.getBulkWriteOperations();
        BulkWriteOptions options = new BulkWriteOptions().ordered(op.getIsOrdered());
        return collection.bulkWrite(operations, options);
    }

    /**
     * Drops the specified collection from the given database. Returns true if the collection
     * was successfully dropped (i.e., is no longer accessible), otherwise false.
     *
     * @param op a {@link DropCollectionOperation} containing the database and collection names
     * @return true if the collection was dropped, otherwise false
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public boolean dropCollection(DropCollectionOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());
        collection.drop();

        // Check that it was dropped
        try {
            getCollection(database, op.getCollectionName(), true);
        } catch (MissingCollectionException e) {
            return true;
        }
        return false;
    }

    /**
     * Creates a new collection in the specified database. Returns true if the collection
     * was created successfully, otherwise false.
     *
     * @param op a {@link CreateCollectionOperation} containing the database name and new collection name
     * @return true if the collection was created, otherwise false
     * @throws MissingDatabaseException if the database doesn't exist and validation is required
     */
    public boolean createCollection(CreateCollectionOperation op) throws MissingDatabaseException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        database.createCollection(op.getCollectionName());

        // Check that it was created
        try {
            getCollection(database, op.getCollectionName(), true);
        } catch (MissingCollectionException e) {
            return false;
        }
        return true;
    }

    /**
     * Creates an index on the specified collection. Returns the name of the newly created index.
     *
     * @param op a {@link CreateIndexOperation} containing the database, collection, and index document
     * @return the name of the newly created index
     * @throws MissingDatabaseException   if the database doesn't exist and validation is required
     * @throws MissingCollectionException if the collection doesn't exist and validation is required
     */
    public String createIndex(CreateIndexOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());
        return collection.createIndex(op.getIndexDocument());
    }

    /**
     * Builds a {@link Document} summarizing the result of an update operation,
     * including matchedCount, modifiedCount, and upsertedId.
     *
     * @param updateResult an {@link UpdateResult} from MongoDB
     * @return a {@link Document} describing the update outcome
     */
    private Document getDocumentFromUpdateResult(UpdateResult updateResult) {
        Document result = new Document();
        result.put("matchedCount", updateResult.getMatchedCount());
        result.put("modifiedCount", updateResult.getModifiedCount());
        result.put("upsertedId", updateResult.getUpsertedId());
        return result;
    }

    /**
     * Builds a {@link Document} summarizing the result of a delete operation,
     * including whether it was acknowledged and how many documents were deleted.
     *
     * @param deleteResult a {@link DeleteResult} from MongoDB
     * @return a {@link Document} describing the delete outcome
     */
    private Document getDocumentFromDeleteResult(DeleteResult deleteResult) {
        Document result = new Document();
        result.put("acknowledged", deleteResult.wasAcknowledged());
        result.put("deletedCount", deleteResult.getDeletedCount());
        return result;
    }

    /**
     * Converts a string-based read preference into a {@link ReadPreference} object.
     * Returns null if the given string does not match a recognized read preference.
     *
     * @param readPreference a string representing the desired read preference
     * @return a corresponding {@link ReadPreference}, or null if none matched
     */
    private ReadPreference getReadPreference(String readPreference) {
        switch (readPreference) {
            case MongoDbConnectedSystemConstants.READ_PREFERENCE_PRIMARY:
                return ReadPreference.primary();
            case MongoDbConnectedSystemConstants.READ_PREFERENCE_PRIMARY_PREFERRED:
                return ReadPreference.primaryPreferred();
            case MongoDbConnectedSystemConstants.READ_PREFERENCE_SECONDARY:
                return ReadPreference.secondary();
            case MongoDbConnectedSystemConstants.READ_PREFERENCE_SECONDARY_PREFERRED:
                return ReadPreference.secondaryPreferred();
            case MongoDbConnectedSystemConstants.READ_PREFERENCE_NEAREST:
                return ReadPreference.nearest();
            default:
                return null;
        }
    }

    /**
     * Converts a string-based read concern into a {@link ReadConcern} object.
     * Defaults to {@link ReadConcern#DEFAULT} if the specified string is unrecognized.
     *
     * @param readConcern a string representing the desired read concern
     * @return a corresponding {@link ReadConcern}, or {@link ReadConcern#DEFAULT} if none matched
     */
    private ReadConcern getReadConcern(String readConcern) {
        switch (readConcern) {
            case MongoDbConnectedSystemConstants.READ_CONCERN_AVAILABLE:
                return ReadConcern.AVAILABLE;
            case MongoDbConnectedSystemConstants.READ_CONCERN_LINEARIZABLE:
                return ReadConcern.LINEARIZABLE;
            case MongoDbConnectedSystemConstants.READ_CONCERN_LOCAL:
                return ReadConcern.LOCAL;
            case MongoDbConnectedSystemConstants.READ_CONCERN_MAJORITY:
                return ReadConcern.MAJORITY;
            case MongoDbConnectedSystemConstants.READ_CONCERN_SNAPSHOT:
                return ReadConcern.SNAPSHOT;
            default:
                return ReadConcern.DEFAULT;
        }
    }
}