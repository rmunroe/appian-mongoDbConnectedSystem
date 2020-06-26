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
//    public static final String MASTER_KEY = "MASTER_KEY";
//    public static final String KEY_VAULT_NAMESPACE = "KEY_VAULT_NAMESPACE";

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

//                encryptedTextProperty(MASTER_KEY)
//                        .label("Encryption Master Key")
//                        .description("Optional. This is the Master Key that was used to create the encryption key on your MongoDB instance.")
//                        .isImportCustomizable(true)
//                        .masked(true)
//                        .isRequired(false)
//                        .build(),
//
//                encryptedTextProperty(KEY_VAULT_NAMESPACE)
//                        .label("Key Vault Namespace")
//                        .description("Optional. This is the Key Vault Namespace for the encryption key on your MongoDB instance.")
//                        .placeholder("admin.datakeys")
//                        .isImportCustomizable(true)
//                        .masked(true)
//                        .isRequired(false)
//                        .build()
        );
    }


    @Override
    protected TestConnectionResult testConnection(SimpleConfiguration configuration, ExecutionContext executionContext) {
        try {
            MongoClient mongoClient = MongoDbConnection.instance.get(configuration.getValue(CONNECTION_STRING));
            assert MongoDbConnection.testMongoDbConnection(mongoClient);
        } catch (Exception e) {
            String message = e.getClass().getSimpleName() + "\n\n" + e.getMessage();
            if (e.getCause() != null)
                message += "\n\nCaused by: " + e.getCause().getMessage();
            return TestConnectionResult.error(message);
        }
        return TestConnectionResult.success();
    }
}