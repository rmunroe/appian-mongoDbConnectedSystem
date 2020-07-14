package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.solutionsconsulting.plugin.mongodb.ConnectedSystemUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.IntegrationUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbUtility;
import com.appiancorp.solutionsconsulting.plugin.mongodb.PropertyDescriptorsUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.CollectionAggregateOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;


public class AggregateIntegrationTemplate extends MongoDbIntegrationTemplate {
    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {

        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        List<PropertyDescriptor<?>> propertyDescriptors = new ArrayList<>();
        PropertyDescriptorsUtil propertyDescriptorsUtil = new PropertyDescriptorsUtil(this, integrationConfiguration, mongoDbUtility, propertyDescriptors);

        if (this.isWriteOperation())
            propertyDescriptorsUtil.buildFileOutputProperty(); // Show file output settings
        else
            propertyDescriptorsUtil.buildOutputTypeProperty(); // Show Dictionary / JSON list

        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        if (integrationConfiguration.getValue(COLLECTION) != null) {
            propertyDescriptorsUtil.buildCollectionAggregateProperties();

            propertyDescriptorsUtil.buildCollationsProperty();
            propertyDescriptorsUtil.buildReadPreferenceProperty();
            propertyDescriptorsUtil.buildReadConcernProperty();
        }

        return integrationConfiguration.setProperties(
                propertyDescriptors.toArray(new PropertyDescriptor[0])
        ).setValue(
                "ExamplePipeline",
                "a!toJson({{'$group':{'_id':\"$lastName\",count:{'$sum':1}}},{'$sort':{count:-1,'_id':1}},{'$limit':10}})"
        );
    }

    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext) {

        ConnectedSystemUtil csUtil = new ConnectedSystemUtil("MongoCollection.aggregate()");
        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        IntegrationUtil integrationUtil = new IntegrationUtil(integrationConfiguration, executionContext);

        CollectionAggregateOperation aggregateOperation;
        try {
            aggregateOperation = new CollectionAggregateOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),
                    integrationConfiguration.getValue(READ_PREFERENCE),
                    integrationConfiguration.getValue(READ_CONCERN),

                    integrationConfiguration.getValue(OUTPUT_TYPE),
                    integrationConfiguration.getValue(AGGREGATE_PIPELINE_JSON),

                    integrationUtil.buildCollation()
            );
        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.addAllRequestDiagnostic(aggregateOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", aggregateOperation.getDatabaseName());
        output.put("collection", aggregateOperation.getCollectionName());

        try {
            if (this.isWriteOperation()) {
                // Export to JSON file
                List<String> jsonList = mongoDbUtility.aggregateJson(aggregateOperation);
                Document document = integrationUtil.writeJsonListToDocument(jsonList);
                output.put("jsonDocument", document);

            } else if (aggregateOperation.getOutputType() != null && aggregateOperation.getOutputType().equals(OUTPUT_TYPE_JSON_ARRAY)) {
                output.put("documents", mongoDbUtility.aggregateJson(aggregateOperation));

            } else {
                output.put("documents", mongoDbUtility.aggregate(aggregateOperation));
            }

        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }
}
