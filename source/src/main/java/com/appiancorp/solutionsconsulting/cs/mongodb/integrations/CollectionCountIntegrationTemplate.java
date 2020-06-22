package com.appiancorp.solutionsconsulting.cs.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.DisplayHint;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.cs.mongodb.*;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.cs.mongodb.operations.CollectionCountOperation;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoQueryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;


@TemplateId(name = "CollectionCountIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class CollectionCountIntegrationTemplate extends SimpleIntegrationTemplate {
    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {

        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        List<PropertyDescriptor<?>> propertyDescriptors = new ArrayList<>();
        PropertyDescriptorsUtil propertyDescriptorsUtil = new PropertyDescriptorsUtil(this, integrationConfiguration, mongoDbUtility, propertyDescriptors);

        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        if (integrationConfiguration.getValue(COLLECTION) != null) {
            propertyDescriptors.add(textProperty(MongoDbConnectedSystemConstants.FILTER_JSON)
                    .label("Query Filter JSON")
                    .description("A JSON string representing a Collection.Count() filter Document")
                    .instructionText("Leave Query Filter JSON blank to count all Documents in the Collection")
                    .isExpressionable(true)
                    .displayHint(DisplayHint.EXPRESSION)
                    .isRequired(false)
                    .build()
            );

            propertyDescriptorsUtil.buildCollationsProperty();

            propertyDescriptorsUtil.buildReadPreferenceProperty();
            propertyDescriptorsUtil.buildReadConcernProperty();
        }

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }

    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext) {

        ConnectedSystemUtil csUtil = new ConnectedSystemUtil("MongoCollection.count()");
        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        IntegrationUtil integrationUtil = new IntegrationUtil(integrationConfiguration, executionContext);

        CollectionCountOperation op;
        try {
            op = new CollectionCountOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),
                    integrationConfiguration.getValue(READ_PREFERENCE),
                    integrationConfiguration.getValue(READ_CONCERN),

                    integrationConfiguration.getValue(FILTER_JSON),

                    integrationUtil.buildCollation()
            );
        } catch (InvalidJsonException e) {
            return csUtil.buildApiExceptionError(
                    e.getMessage(),
                    "Invalid JSON string: \"" + e.jsonString + "\"");
        }

        csUtil.addAllRequestDiagnostic(op.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", op.getDatabaseName());
        output.put("collection", op.getCollectionName());

        try {
            output.put("count", mongoDbUtility.count(op));
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
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }
}