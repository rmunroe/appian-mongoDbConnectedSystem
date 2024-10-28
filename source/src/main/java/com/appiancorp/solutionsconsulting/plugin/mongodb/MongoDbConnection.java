package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang.StringUtils;
//import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MongoDbConnection {
//    private static final Logger LOG = Logger.getLogger(MongoDbConnection.class);
    // MongoClient instances are stored in a singleton HashMap, tied to unique connection string
    private static final Map<String, MongoClient> mongoMap = new HashMap<>();
    public static MongoDbConnection instance = new MongoDbConnection();


    private MongoDbConnection() {
    }

    public static Boolean testMongoDbConnection(MongoClient mongo) {
        return StringUtils.isNotEmpty(mongo.listDatabaseNames().first());
    }

    public static String cleanConnectionString(String connectionString) {
        return connectionString.replaceFirst("^(.*//.*?:).*?(@.*)$", "$1<password>$2");
    }

    public MongoClient get(String connectionString) throws RuntimeException {
        MongoClient mongo;

        if (mongoMap.containsKey(connectionString))
            return mongoMap.get(connectionString);
        else {
            mongo = MongoClients.create(new ConnectionString(connectionString));
            if (testMongoDbConnection(mongo)) {
                mongoMap.put(connectionString, mongo);
                return mongo;
            }

            throw new RuntimeException("Could not connect to MongoDB");
        }
    }
}
