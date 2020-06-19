package com.appiancorp.solutionsconsulting.cs.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.BooleanDisplayMode;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.cs.mongodb.ConnectedSystemUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.cs.mongodb.PropertyDescriptorsUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants;
import com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;


@TemplateId(name = "ListCollectionsIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class ListCollectionsIntegrationTemplate extends SimpleIntegrationTemplate {

    public final static String UUID_STRING = "UUID_STRING";

    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {

        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        List<PropertyDescriptor<?>> propertyDescriptors = new ArrayList<>();
        PropertyDescriptorsUtil propertyDescriptorsUtil = new PropertyDescriptorsUtil(this, integrationConfiguration, mongoDbUtility, propertyDescriptors);

        propertyDescriptorsUtil.buildOutputTypeProperty();
        propertyDescriptorsUtil.buildDatabaseProperty();

        String outputType = integrationConfiguration.getValue(OUTPUT_TYPE);
        if (outputType != null && outputType.equals(OUTPUT_TYPE_DICTIONARY)) {
            propertyDescriptors.add(booleanProperty(UUID_STRING)
                    .label("UUIDs as Strings")
                    .description("Ensure any UUIDs in the result are converted to Strings")
                    .displayMode(BooleanDisplayMode.RADIO_BUTTON)
                    .isExpressionable(true)
                    .build()
            );
        }

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }


    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext) {

        ConnectedSystemUtil csUtil = new ConnectedSystemUtil("MongoDatabase.listCollections()");
        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);

        String outputType = integrationConfiguration.getValue(OUTPUT_TYPE);
        String databaseName = integrationConfiguration.getValue(DATABASE);
        Boolean validateDatabase = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.DATABASE_EXISTS);
        Boolean uuidAsString = integrationConfiguration.getValue(UUID_STRING);

        csUtil.addAllRequestDiagnostic(getRequestDiagnostic(outputType, databaseName, uuidAsString));

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", databaseName);

        try {
            if (outputType != null && outputType.equals(OUTPUT_TYPE_JSON_ARRAY)) {
                output.put("collections", mongoDbUtility.listCollectionsJson(databaseName, validateDatabase));
            } else {
                output.put("collections", mongoDbUtility.listCollections(databaseName, validateDatabase, uuidAsString));
            }
        } catch (MissingDatabaseException e) {
            return csUtil.buildApiExceptionError(
                    "Missing Database Exception",
                    e.getMessage());
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }


    private Map<String, Object> getRequestDiagnostic(String outputType, String databaseName, Boolean uuidAsString) {
        Map<String, Object> diagnostic = new HashMap<>();
        diagnostic.put("Output Type", outputType);
        diagnostic.put("Database", databaseName);
        diagnostic.put("UUIDs as Strings", uuidAsString);
        return diagnostic;
    }
}
