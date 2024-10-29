package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MongoDbConnection {
    private static final Logger LOG = (Logger) LogManager.getLogger(MongoDbConnection.class);
    // MongoClient instances are stored in a singleton HashMap, tied to unique connection string
    private static final Map<String, MongoClient> mongoMap = new HashMap<>();


    private MongoDbConnection() {
    }

    public static Boolean testMongoDbConnection(MongoClient mongo) {
        return StringUtils.isNotEmpty(mongo.listDatabaseNames().first());
    }

    public static String cleanConnectionString(String connectionString) {
        return connectionString.replaceFirst("^(.*//.*?:).*?(@.*)$", "$1<password>$2");
    }

    public static MongoClient get(String connectionString) throws RuntimeException {
        // Check the singleton Map if we have a client for this connection string
        if (mongoMap.containsKey(connectionString))
            return mongoMap.get(connectionString);
        else {
            // If not, create one, test it, and store it
            MongoClient mongo = MongoClients.create(MongoClientSettings.builder()
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(10000, MILLISECONDS);
                        builder.readTimeout(10000, MILLISECONDS);
                    })
                    .applyToClusterSettings(builder -> builder.serverSelectionTimeout(10000, MILLISECONDS))
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build()
            );
            if (testMongoDbConnection(mongo)) {
                mongoMap.put(connectionString, mongo);
                return mongo;
            }

            LOG.error("Could not connect to MongoDB!");
            throw new RuntimeException("Could not connect to MongoDB");
        }
    }
}
