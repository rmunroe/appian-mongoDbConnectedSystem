package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.plugin.mongodb.ConnectedSystemUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.IntegrationUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbUtility;
import com.appiancorp.solutionsconsulting.plugin.mongodb.PropertyDescriptorsUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.CollectionCountOperation;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoQueryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;


@TemplateId(name = "CountIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class CountIntegrationTemplate extends SimpleIntegrationTemplate {
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
            propertyDescriptorsUtil.buildFilterJsonProperty(false);

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
