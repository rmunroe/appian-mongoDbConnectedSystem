package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;
import com.mongodb.client.MongoClient;

/**
 * A connected system template for creating a MongoDB connection within Appian.
 * This class extends {@link SimpleTestableConnectedSystemTemplate} to enable
 * testing the MongoDB connection via the Appian interface.
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Define configuration properties, such as the connection string, via
 *       {@link #getConfiguration(SimpleConfiguration, ExecutionContext)}.</li>
 *   <li>Perform a test connection through {@link #testConnection(SimpleConfiguration, ExecutionContext)}
 *       to validate connectivity to MongoDB.</li>
 * </ul>
 *
 * <p><b>Note:</b> The {@code connection string} is stored in a masked property
 * to protect credentials.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
@TemplateId(name = "MongoDbConnectedSystemTemplate")
public class MongoDbConnectedSystemTemplate extends SimpleTestableConnectedSystemTemplate {

    /**
     * The key used to retrieve or store the MongoDB connection string in the Appian
     * {@link SimpleConfiguration}.
     */
    public static final String CONNECTION_STRING = "CONNECTION_STRING";

    /**
     * Defines the configuration properties for this connected system, including
     * the MongoDB connection string. The string is encrypted and masked in the
     * Appian Designer to protect credentials.
     *
     * @param simpleConfiguration the existing configuration object to update
     * @param executionContext    provides context about the current execution environment
     * @return a {@link SimpleConfiguration} with the MongoDB connection string property
     */
    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {

        return simpleConfiguration.setProperties(
                encryptedTextProperty(CONNECTION_STRING)
                        .label("Connection String")
                        .description("The complete Connection String to the MongoDB server or cluster, including " +
                                "authentication details and any client configuration parameters you need. " +
                                "See the MongoDB 4.0 Java Driver documentation for complete details on " +
                                "Connection Strings.")
                        .placeholder("mongodb+srv://username:password@cluster0-abcde.mongodb.net/admin")
                        .instructionText("This field is masked as the login credentials are visible within the " +
                                "Connection String, as well as stored using Appian's internal encryption.")
                        .isImportCustomizable(true)
                        .masked(true)
                        .isRequired(true)
                        .build()
        );
    }

    /**
     * Tests the MongoDB connection using the provided connection string. The
     * connection attempt is wrapped in a try-catch to return an appropriate
     * {@link TestConnectionResult} based on success or failure.
     *
     * @param configuration    a {@link SimpleConfiguration} containing the connection string
     * @param executionContext provides context about the current execution environment
     * @return a {@link TestConnectionResult} indicating success or failure, based on
     *         whether the client can connect to MongoDB
     */
    @Override
    protected TestConnectionResult testConnection(SimpleConfiguration configuration, ExecutionContext executionContext) {
        try {
            MongoClient mongoClient = MongoDbConnection.get(configuration.getValue(CONNECTION_STRING));
            if (MongoDbConnection.testMongoDbConnection(mongoClient)) {
                return TestConnectionResult.success();
            }
        } catch (Exception e) {
            String message = e.getClass().getSimpleName() + "\n\n" + e.getMessage();
            if (e.getCause() != null) {
                message += "\n\nCaused by: " + e.getCause().getMessage();
            }
            return TestConnectionResult.error(message);
        }
        return TestConnectionResult.error("Could not connect to MongoDB");
    }
}