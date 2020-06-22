package com.appiancorp.solutionsconsulting.cs.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.configuration.DisplayHint;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.cs.mongodb.operations.UpdateOperation;
import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;

import java.util.HashMap;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;


@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class UpdateIntegrationTemplate extends MongoDbIntegrationTemplate {
    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext
    ) {
        this.setupConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);

        propertyDescriptorsUtil.buildOutputTypeProperty();
        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        if (integrationConfiguration.getValue(COLLECTION) != null) {
            propertyDescriptorsUtil.buildFilterJsonProperty();

            propertyDescriptors.add(TextPropertyDescriptor.builder()
                    .key(UPDATE_JSON)
                    .label("Update Instructions JSON")
                    .description("A JSON string representing how the MongoDB Document should be updated")
                    .isExpressionable(true)
                    .displayHint(DisplayHint.EXPRESSION)
                    .isRequired(true)
                    .build()
            );

            propertyDescriptorsUtil.buildInsertOptionsProperties();
        }

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }


    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext
    ) {
        String apiMethodName;
        if (this.isUpsertOne())
            apiMethodName = "MongoCollection.updateOne()";
        else
            apiMethodName = "MongoCollection.updateMany()";

        this.setupExecute(apiMethodName, integrationConfiguration, connectedSystemConfiguration, executionContext);

        UpdateOperation updateOperation;
        try {
            updateOperation = new UpdateOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),

                    integrationConfiguration.getValue(OUTPUT_TYPE),
                    integrationConfiguration.getValue(FILTER_JSON),
                    integrationConfiguration.getValue(UPDATE_JSON),

                    integrationConfiguration.getValue(INSERT_SKIP_DATETIME_CONVERSION)
            );
        } catch (InvalidJsonException e) {
            return csUtil.buildApiExceptionError(
                    e.getMessage(),
                    "Invalid JSON string: \"" + e.jsonString + "\"");
        }

        csUtil.addAllRequestDiagnostic(updateOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", updateOperation.getDatabaseName());
        output.put("collection", updateOperation.getCollectionName());

        try {
            if (this.isUpsertOne())
                output.put("updateResult", mongoDbUtility.updateOne(updateOperation));
            else
                output.put("updateResult", mongoDbUtility.updateMany(updateOperation));

        } catch (MongoExecutionTimeoutException ex) {
            return csUtil.buildApiExceptionError(
                    "Max Processing Time Exceeded",
                    ex.getMessage());
        } catch (MissingCollectionException e) {
            return csUtil.buildApiExceptionError(
                    "Missing Collection Exception",
                    e.getMessage());
        } catch (MissingDatabaseException e) {
            return csUtil.buildApiExceptionError(
                    "Missing Database Exception",
                    e.getMessage());
        } catch (UnsupportedOperationException e) {
            return csUtil.buildApiExceptionError(
                    "Unsupported Operation Exception",
                    e.getMessage());
        } catch (MongoException e) {
            return csUtil.buildApiExceptionError(
                    "Mongo Exception",
                    e.getMessage());
        } catch (Exception e) {
            return csUtil.buildApiExceptionError(
                    "Exception",
                    e.getMessage());
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }
}