package com.appiancorp.solutionsconsulting.cs.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.cs.mongodb.ConnectedSystemUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.cs.mongodb.IntegrationUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbUtility;
import com.appiancorp.solutionsconsulting.cs.mongodb.Operations.CollectionAggregateOperation;
import com.appiancorp.solutionsconsulting.cs.mongodb.PropertyDescriptorsUtil;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoQueryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;

@TemplateId(name = "ExportCollectionAggregateIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class ExportCollectionAggregateIntegrationTemplate extends SimpleIntegrationTemplate {
    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {

        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        List<PropertyDescriptor<?>> propertyDescriptors = new ArrayList<>();
        PropertyDescriptorsUtil propertyDescriptorsUtil = new PropertyDescriptorsUtil(this, integrationConfiguration, mongoDbUtility, propertyDescriptors);

        propertyDescriptorsUtil.buildFileOutputProperty();
        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        Object collectionName = integrationConfiguration.getValue(COLLECTION);
        if (collectionName != null) {
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
        } catch (InvalidJsonException e) {
            return csUtil.buildApiExceptionError(
                    e.getMessage(),
                    "Invalid JSON string: \"" + e.jsonString + "\"");
        }

        csUtil.addAllRequestDiagnostic(aggregateOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", aggregateOperation.getDatabaseName());
        output.put("collection", aggregateOperation.getCollectionName());

        try {
            List<String> jsons = mongoDbUtility.aggregateJson(aggregateOperation);
            Document document = integrationUtil.writeJsonsToDocument(jsons);
            output.put("jsonDocument", document);
        } catch (MongoExecutionTimeoutException e) {
            return csUtil.buildApiExceptionError(
                    "Max Processing Time Exceeded",
                    e.getMessage());
        } catch (MongoQueryException e) {
            return csUtil.buildApiExceptionError(
                    "Query Exception",
                    e.getMessage());
        } catch (MissingCollectionException e) {
            return csUtil.buildApiExceptionError(
                    "Missing Collection Exception",
                    e.getMessage());
        } catch (MissingDatabaseException e) {
            return csUtil.buildApiExceptionError(
                    "Missing Database Exception",
                    e.getMessage());
        } catch (MongoCommandException e) {
            return csUtil.buildApiExceptionError(
                    "Mongo Command Exception",
                    e.getMessage());
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }
}
