package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;
import com.mongodb.client.MongoClient;

@TemplateId(name = "MongoDbConnectedSystemTemplate")
public class MongoDbConnectedSystemTemplate extends SimpleTestableConnectedSystemTemplate {

    public static final String CONNECTION_STRING = "CONNECTION_STRING";

    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {

        return simpleConfiguration.setProperties(
                encryptedTextProperty(CONNECTION_STRING)
                        .label("Connection String")
                        .description("The complete Connection String to the MongoDB server or cluster, including authentication details and any client configuration parameters you need. See the MongoDB 4.0 Java Driver documentation for complete details on Connection Strings.")
                        .placeholder("mongodb+srv://username:password@cluster0-abcde.mongodb.net/admin")
                        .instructionText("This field is masked as the login credentials are visible within the Connection String, as well as stored using Appian's internal encryption.")
                        .isImportCustomizable(true)
                        .masked(true)
                        .isRequired(true)
                        .build()
        );
    }


    @Override
    protected TestConnectionResult testConnection(SimpleConfiguration configuration, ExecutionContext executionContext) {
        try {
            MongoClient mongoClient = MongoDbConnection.get(configuration.getValue(CONNECTION_STRING));
            if (MongoDbConnection.testMongoDbConnection(mongoClient))
                return TestConnectionResult.success();

        } catch (Exception e) {
            String message = e.getClass().getSimpleName() + "\n\n" + e.getMessage();
            if (e.getCause() != null)
                message += "\n\nCaused by: " + e.getCause().getMessage();
            return TestConnectionResult.error(message);
        }
        return TestConnectionResult.error("Could not connect to MongoDB");
    }
}