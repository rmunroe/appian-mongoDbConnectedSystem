package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.CollectionFindOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;


public class FindIntegrationTemplate extends MongoDbIntegrationTemplate {

    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {

        this.setupConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);

        if (this.isWriteOperation())
            propertyDescriptorsUtil.buildFileOutputProperty(); // Show file output settings
        else
            propertyDescriptorsUtil.buildOutputTypeProperty(); // Show Dictionary / JSON list

        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        if (integrationConfiguration.getValue(COLLECTION) != null) {
            propertyDescriptorsUtil.buildCollectionFindProperties();
        }

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }


    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext) {

        this.setupExecute("MongoCollection.find()", integrationConfiguration, connectedSystemConfiguration, executionContext);

        CollectionFindOperation findOperation;
        try {
            findOperation = new CollectionFindOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),
                    integrationConfiguration.getValue(READ_PREFERENCE),
                    integrationConfiguration.getValue(READ_CONCERN),

                    integrationConfiguration.getValue(OUTPUT_TYPE),
                    integrationConfiguration.getValue(FILTER_JSON),
                    integrationConfiguration.getValue(SORT_JSON),
                    integrationConfiguration.getValue(PROJECTION_JSON),

                    integrationConfiguration.getValue(LIMIT) != null ? integrationConfiguration.getValue(LIMIT) : 0,
                    integrationConfiguration.getValue(SKIP) != null ? integrationConfiguration.getValue(SKIP) : 0,
                    integrationConfiguration.getValue(RECORD_ID),
                    integrationConfiguration.getValue(MAX_TIME),

                    integrationUtil.buildCollation()
            );
        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.addAllRequestDiagnostic(findOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", findOperation.getDatabaseName());
        output.put("collection", findOperation.getCollectionName());

        try {
            if (this.isWriteOperation()) {
                // Export to JSON file
                List<String> jsonList = mongoDbUtility.findJson(findOperation);
                Document document = integrationUtil.writeJsonListToDocument(jsonList);
                output.put("jsonDocument", document);

            } else if (findOperation.getOutputType() != null && findOperation.getOutputType().equals(OUTPUT_TYPE_JSON_ARRAY)) {
                output.put("documents", mongoDbUtility.findJson(findOperation));

            } else {
                output.put("documents", mongoDbUtility.find(findOperation));
            }

        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }
}
