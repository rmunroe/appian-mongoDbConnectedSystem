package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.*;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.client.*;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class MongoDbUtility {
    private final MongoClient mongoClient;

    public MongoDbUtility(SimpleConfiguration connectedSystemConfiguration) {
        this.mongoClient = MongoDbConnection.instance.get(connectedSystemConfiguration.getValue(MongoDbConnectedSystemTemplate.CONNECTION_STRING));
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
            results.add(MongoDocumentUtil.prepDocumentForOutput(doc, false, uuidAsString));
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
            results.add(MongoDocumentUtil.prepDocumentForOutput(doc, true, false));
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
//        Consumer<Document> documentConsumer = results::add;

//        aggregateExec(op).forEach(documentConsumer);

        for (Document doc : aggregateExec(op)) {
            results.add(MongoDocumentUtil.prepDocumentForOutput(doc, true, false));
        }

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


    public List<Document> insertMany(InsertManyOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        List<Document> documents = op.getDocuments();
        collection.insertMany(documents);

        List<Document> updated = new ArrayList<>();
        documents.forEach(doc -> updated.add(MongoDocumentUtil.prepDocumentForOutput(doc, true, true)));

        return updated;
    }


    public Document insertOne(InsertOneOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        Document document = op.getDocument();
        collection.insertOne(document);

        return MongoDocumentUtil.prepDocumentForOutput(document, true, true);
    }


    public Document updateOne(UpdateOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        return getDocumentFromUpdateResult(collection.updateOne(op.getFilterDocument(), op.getUpdateDocument()));
    }


    public Document updateMany(UpdateOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        return getDocumentFromUpdateResult(collection.updateMany(op.getFilterDocument(), op.getUpdateDocument()));
    }


    public Document replaceOne(ReplaceOneOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        return getDocumentFromUpdateResult(collection.replaceOne(op.getFilterDocument(), op.getReplacementDocument()));
    }


    public Document deleteOne(DeleteOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        DeleteOptions options = new DeleteOptions();
        if (op.getCollation() != null)
            options.collation(op.getCollation());

        return getDocumentFromDeleteResult(collection.deleteOne(op.getFilterDocument(), options));
    }


    public Document deleteMany(DeleteOperation op) throws MissingDatabaseException, MissingCollectionException {
        MongoDatabase database = getDatabase(op.getDatabaseName(), op.getValidateDatabase());
        MongoCollection<Document> collection = getCollection(database, op.getCollectionName(), op.getValidateCollection());

        DeleteOptions options = new DeleteOptions();
        if (op.getCollation() != null)
            options.collation(op.getCollation());

        return getDocumentFromDeleteResult(collection.deleteMany(op.getFilterDocument()));
    }


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


    private Document getDocumentFromUpdateResult(UpdateResult updateResult) {
        Document result = new Document();
        result.put("matchedCount", updateResult.getMatchedCount());
        result.put("modifiedCount", updateResult.getModifiedCount());
        result.put("upsertedId", updateResult.getUpsertedId());
        return result;
    }

    private Document getDocumentFromDeleteResult(DeleteResult deleteResult) {
        Document result = new Document();
        result.put("acknowledged", deleteResult.wasAcknowledged());
        result.put("deletedCount", deleteResult.getDeletedCount());
        return result;
    }


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
