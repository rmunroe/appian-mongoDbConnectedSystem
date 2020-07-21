package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MongoDbConnection {
    private static final Logger LOG = Logger.getLogger(MongoDbConnection.class);

    // MongoClient instances are stored in a singleton HashMap, tied to unique, hashed connection string
    private static final Map<String, MongoClient> mongoMap = new HashMap<>();


    public static MongoDbConnection instance = new MongoDbConnection();


    private MongoDbConnection() {
    }


    public MongoClient getClientFor(String connectionString) throws RuntimeException {
        LOG.debug("Connecting to: " + cleanConnectionString(connectionString));

        MongoClient mongo;

        String mapKey;
        try {
            mapKey = getHashedConnectionString(connectionString);
        } catch (NoSuchAlgorithmException ignored) {
            LOG.warn("SHA-256 not supported on this system! Please use a supported version of Java.");
            mapKey = connectionString;
        }

        if (mongoMap.containsKey(mapKey)) {
            LOG.debug("Client instance for this connection string existed. Returning.");
            return mongoMap.get(mapKey);
        } else {
            LOG.debug("Creating new client instance for this connection string.");
            mongo = MongoClients.create(new ConnectionString(connectionString));
            if (testMongoDbConnection(mongo)) {
                mongoMap.put(mapKey, mongo);
                return mongo;
            }

            LOG.error("Could not connect to MongoDB. Throwing RuntimeException.");
            throw new RuntimeException("Could not connect to MongoDB");
        }
    }


    public static Boolean testMongoDbConnection(MongoClient mongo) {
        return StringUtils.isNotEmpty(mongo.listDatabaseNames().first());
    }


    public static String cleanConnectionString(String connectionString) {
        return connectionString.replaceFirst("^(.*//.*?:).*?(@.*)$", "$1<password>$2");
    }


    public static String getHashedConnectionString(String connectionString) throws NoSuchAlgorithmException {
        StringBuilder hexString = new StringBuilder();

        for (byte b : MessageDigest.getInstance("SHA-256").digest(connectionString.getBytes(StandardCharsets.UTF_8))) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
