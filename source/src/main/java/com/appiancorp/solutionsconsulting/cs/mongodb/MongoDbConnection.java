package com.appiancorp.solutionsconsulting.cs.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.log4j.Logger;
import org.bson.UuidRepresentation;

import java.util.HashMap;
import java.util.Map;

public class MongoDbConnection {
    private static final Logger LOG = Logger.getLogger(MongoDbConnection.class);

    public static MongoDbConnection instance = new MongoDbConnection();

    // MongoClient instances are stored in a singleton HashMap, tied to unique connection string
    private Map<String, MongoClient> mongoMap = new HashMap<>();


    private MongoDbConnection() {
    }


    public MongoClient get(String connectionString) throws RuntimeException {
        MongoClient mongo = null;

        if (!mongoMap.containsKey(connectionString) || mongoMap.get(connectionString).listDatabaseNames().first() == null) {
            try {
                ConnectionString cs = new ConnectionString(connectionString);

                MongoClientSettings mcs = MongoClientSettings.builder()
                        .applyConnectionString(cs)
//                        .applyToConnectionPoolSettings()
                        .build();

                mongo = MongoClients.create(mcs);

                mongoMap.put(connectionString, mongo);
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
            }
        } else {
            mongo = mongoMap.get(connectionString);
        }

        return mongo;
    }


    public static String cleanConnectionString(String connectionString) {
        return connectionString.replaceFirst("^(.*//.*?:).*?(@.*)$", "$1<password>$2");
    }
}
