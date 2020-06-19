package com.appiancorp.solutionsconsulting.cs.mongodb;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.cs.mongodb.Operations.CollectionAggregateOperation;
import com.appiancorp.solutionsconsulting.cs.mongodb.Operations.CollectionCountOperation;
import com.appiancorp.solutionsconsulting.cs.mongodb.Operations.CollectionFindOperation;
import com.mongodb.Block;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.client.*;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;
import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemTemplate.CONNECTION_STRING;


public class MongoDbUtility {
    private final MongoClient mongoClient;

    public MongoDbUtility(SimpleConfiguration connectedSystemConfiguration) {
        this.mongoClient = MongoDbConnection.instance.get(connectedSystemConfiguration.getValue(CONNECTION_STRING));
    }


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


    private MongoCollection<Document> getCollection(MongoDatabase database, String collectionName, Boolean validate) throws MissingCollectionException {
        if (validate != null && validate) {
            List<String> collectionNames = new ArrayList<>();
            database.listCollectionNames().into(collectionNames);
            if (!collectionNames.contains(collectionName)) {
                throw new MissingCollectionException(collectionName);
            }
        }
        return database.getCollection(collectionName);
    }


    public List<Map<String, Object>> listDatabases() {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Document doc : this.mongoClient.listDatabases()) {
            results.add(doc);
        }

        return results;
    }

    public List<String> listDatabasesJson() {
        List<String> results = new ArrayList<>();

        for (Document doc : this.mongoClient.listDatabases()) {
            results.add(doc.toJson());
        }

        return results;
    }


    public List<Map<String, Object>> listCollections(String databaseName, Boolean validateDatabase, Boolean uuidAsString) throws MissingDatabaseException {
        MongoDatabase database = getDatabase(databaseName, validateDatabase);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Document doc : database.listCollections()) {
            results.add(prepDocumentForOutput(doc, false, uuidAsString));
        }

        return results;
    }

    public List<String> listCollectionsJson(String databaseName, Boolean validateDatabase) throws MissingDatabaseException {
        MongoDatabase database = getDatabase(databaseName, validateDatabase);
        List<String> results = new ArrayList<>();

        for (Document doc : database.listCollections()) {
            results.add(doc.toJson());
        }

        return results;
    }


    public List<Map<String, Object>> find(CollectionFindOperation op) throws MissingDatabaseException, MissingCollectionException {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Document doc : execFind(op)) {
            results.add(prepDocumentForOutput(doc, true, false));
        }

        return results;
    }

    public List<String> findJson(CollectionFindOperation op) throws MissingDatabaseException, MissingCollectionException {
        List<String> results = new ArrayList<>();

        for (Document doc : execFind(op)) {
            results.add(doc.toJson());
        }

        return results;
    }

    private FindIterable<Document> execFind(CollectionFindOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        if (StringUtils.isNotEmpty(op.getReadPreference())) {
            collection = collection.withReadPreference(Objects.requireNonNull(getReadPreference(op.getReadPreference())));
        }
        if (StringUtils.isNotEmpty(op.getReadConcern())) {
            collection = collection.withReadConcern(getReadConcern(op.getReadConcern()));
        }

        FindIterable<Document> results = collection.find(op.getFilterDocument());

        if (op.getSortDocument() != null) results.sort(op.getSortDocument());
        if (op.getProjectionDocument() != null) results.projection(op.getProjectionDocument());
        if (op.getCollation() != null) results.collation(op.getCollation());
        if (op.getLimit() != null) results.limit(op.getLimit());
        if (op.getSkip() != null) results.skip(op.getSkip());
        if (op.getIncludeRecordId() != null && op.getIncludeRecordId()) results.showRecordId(true);
        if (op.getMaxTime() != null) results.maxTime(op.getMaxTime(), TimeUnit.MILLISECONDS);

        return results;
    }


    public long count(CollectionCountOperation op) throws MissingDatabaseException, MissingCollectionException {
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


    public List<Map<String, Object>> aggregate(CollectionAggregateOperation op) throws MissingDatabaseException, MissingCollectionException {
        List<Map<String, Object>> results = new ArrayList<>();
        Consumer<Document> documentConsumer = results::add;

        aggregateExec(op).forEach(documentConsumer);

        return results;
    }

    public List<String> aggregateJson(CollectionAggregateOperation op) throws MissingDatabaseException, MissingCollectionException {
        List<String> results = new ArrayList<>();

        for (Document doc : aggregateExec(op)) {
            results.add(doc.toJson());
        }

        return results;
    }

    public AggregateIterable<Document> aggregateExec(CollectionAggregateOperation op) throws MissingDatabaseException, MissingCollectionException {
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
     * Cleans up any oddities in the Document, such as better handling of ObjectIds and UUIDs
     *
     * @param document A org.bson.Document
     * @return A cleaned HashMap
     */
    public Document prepDocumentForOutput(Document document, Boolean objectIdAsString, Boolean uuidAsString) {
        for (String key : document.keySet()) {
            Object val = document.get(key);

            if (val instanceof Document) {
                document.put(key, prepDocumentForOutput((Document) val, objectIdAsString, uuidAsString)); // recurse

            } else if (objectIdAsString != null && objectIdAsString && val instanceof ObjectId) {
                document.put(key, val.toString());

            } else if (uuidAsString != null && uuidAsString && val instanceof UUID) {
                document.put(key, val.toString());
            }
        }

        return document;
    }


    private ReadPreference getReadPreference(String readPreference) {
        switch (readPreference) {
            case READ_PREFERENCE_PRIMARY:
                return ReadPreference.primary();
            case READ_PREFERENCE_PRIMARYPREFERRED:
                return ReadPreference.primaryPreferred();
            case READ_PREFERENCE_SECONDARY:
                return ReadPreference.secondary();
            case READ_PREFERENCE_SECONDARYPREFERRED:
                return ReadPreference.secondaryPreferred();
            case READ_PREFERENCE_NEAREST:
                return ReadPreference.nearest();
            default:
                return null;
        }
    }

    private ReadConcern getReadConcern(String readConcern) {
        switch (readConcern) {
            case READ_CONCERN_AVAILABLE:
                return ReadConcern.AVAILABLE;
            case READ_CONCERN_LINEARIZABLE:
                return ReadConcern.LINEARIZABLE;
            case READ_CONCERN_LOCAL:
                return ReadConcern.LOCAL;
            case READ_CONCERN_MAJORITY:
                return ReadConcern.MAJORITY;
            case READ_CONCERN_SNAPSHOT:
                return ReadConcern.SNAPSHOT;
            default:
                return ReadConcern.DEFAULT;
        }
    }
}
