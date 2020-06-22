package com.appiancorp.solutionsconsulting.cs.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.DisplayHint;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.cs.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.cs.mongodb.operations.ReplaceOneOperation;
import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;

import java.util.HashMap;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;

@TemplateId(name = "ReplaceOneIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class ReplaceOneIntegrationTemplate extends MongoDbIntegrationTemplate {

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

        propertyDescriptorsUtil.buildFilterJsonProperty(true);

        if (integrationConfiguration.getValue(COLLECTION) != null) {
            propertyDescriptors.add(TextPropertyDescriptor.builder()
                    .key(REPLACE_ONE_JSON)
                    .label("Replacement Mongo Document JSON")
                    .description("A JSON string representing a Document to replace the existing Document")
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
        this.setupExecute("MongoCollection.replaceOne()", integrationConfiguration, connectedSystemConfiguration, executionContext);

        ReplaceOneOperation replaceOneOperation;
        try {
            replaceOneOperation = new ReplaceOneOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),

                    integrationConfiguration.getValue(OUTPUT_TYPE),
                    integrationConfiguration.getValue(FILTER_JSON),
                    integrationConfiguration.getValue(REPLACE_ONE_JSON),

                    integrationConfiguration.getValue(INSERT_SKIP_DATETIME_CONVERSION)
            );
        } catch (InvalidJsonException e) {
            return csUtil.buildApiExceptionError(
                    e.getMessage(),
                    "Invalid JSON string: \"" + e.jsonString + "\"");
        }

        csUtil.addAllRequestDiagnostic(replaceOneOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", replaceOneOperation.getDatabaseName());
        output.put("collection", replaceOneOperation.getCollectionName());

        try {
            output.put("updateResult", mongoDbUtility.replaceOne(replaceOneOperation));

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
