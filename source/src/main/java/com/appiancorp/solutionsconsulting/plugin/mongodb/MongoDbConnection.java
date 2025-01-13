package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Manages MongoDB {@link MongoClient} instances within a static singleton map, keyed by
 * unique connection strings. Provides functionality to create or retrieve a client,
 * and to test connectivity by querying MongoDB for a list of database names.
 *
 * <p>This class is designed to:
 * <ul>
 *   <li>Prevent multiple {@link MongoClient} instances for the same connection string</li>
 *   <li>Allow quick retrieval of already-established connections</li>
 *   <li>Encapsulate basic connectivity checks</li>
 * </ul>
 * </p>
 *
 * <p><b>Thread Safety:</b> Because we’re using a {@link ConcurrentHashMap}, calls to
 * retrieve or create {@link MongoClient} instances are thread-safe.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * MongoClient client = MongoDbConnection.get("mongodb+srv://user:pass@cluster.url");
 * // Use the client to interact with your MongoDB environment...
 * }</pre>
 *
 * <p>If connectivity fails (e.g., bad credentials or host unreachable),
 * a {@link RuntimeException} is thrown.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class MongoDbConnection {

    /**
     * A thread-safe singleton map storing {@link MongoClient} instances keyed by their
     * connection string. If the same connection string is requested multiple
     * times, this map ensures one {@link MongoClient} is shared.
     */
    private static final Map<String, MongoClient> mongoMap = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MongoDbConnection() {
    }

    /**
     * Tests connectivity to MongoDB by retrieving the first database name from the
     * provided {@link MongoClient}.
     *
     * @param mongo the {@link MongoClient} to test
     * @return {@code true} if at least one database name is returned,
     *         {@code false} otherwise
     */
    public static Boolean testMongoDbConnection(MongoClient mongo) {
        return StringUtils.isNotEmpty(mongo.listDatabaseNames().first());
    }

    /**
     * Retrieves an existing {@link MongoClient} for the given connection string if it
     * exists in the singleton map. Otherwise, creates a new client via
     * {@link #createMongoClient(String)}, tests connectivity, and caches it using
     * {@link ConcurrentHashMap#computeIfAbsent(Object, java.util.function.Function)}.
     *
     * <p>If connectivity fails, a {@link RuntimeException} is thrown.</p>
     *
     * @param connectionString the MongoDB connection string, including any authentication
     *                         and configuration parameters
     * @return a {@link MongoClient} instance corresponding to the given connection string
     * @throws RuntimeException if the connection could not be established
     */
    public static MongoClient get(String connectionString) {
        return mongoMap.computeIfAbsent(connectionString, connStr -> {
            MongoClient mongo = createMongoClient(connStr);
            if (!testMongoDbConnection(mongo)) {
                throw new RuntimeException("Could not connect to MongoDB");
            }
            return mongo;
        });
    }

    /**
     * Creates a new {@link MongoClient} with specified timeouts (10s each) for socket
     * and cluster settings, using the provided {@link ConnectionString}.
     *
     * @param connectionString the MongoDB connection string
     * @return a newly created {@link MongoClient} ready for connectivity checks
     */
    private static MongoClient createMongoClient(String connectionString) {
        return MongoClients.create(MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(10000, MILLISECONDS);
                    builder.readTimeout(10000, MILLISECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(10000, MILLISECONDS))
                .applyConnectionString(new ConnectionString(connectionString))
                .build());
    }
}