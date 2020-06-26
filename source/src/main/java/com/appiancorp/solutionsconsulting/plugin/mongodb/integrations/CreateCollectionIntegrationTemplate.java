package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.CreateCollectionOperation;
import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;

import java.util.HashMap;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;


@TemplateId(name = "CreateCollectionIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class CreateCollectionIntegrationTemplate extends MongoDbIntegrationTemplate {
    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext
    ) {
        this.setupConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);

        propertyDescriptorsUtil.buildDatabaseProperty();

        propertyDescriptors.add(textProperty(COLLECTION)
                .label("New Collection Name")
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }


    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext
    ) {
        this.setupExecute("MongoDatabase.createCollection()", integrationConfiguration, connectedSystemConfiguration, executionContext);

        CreateCollectionOperation createCollectionOperation = new CreateCollectionOperation(
                integrationConfiguration.getValue(DATABASE),
                integrationConfiguration.getValue(DATABASE_EXISTS),
                integrationConfiguration.getValue(COLLECTION)
        );

        csUtil.addAllRequestDiagnostic(createCollectionOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", createCollectionOperation.getDatabaseName());
        output.put("collection", createCollectionOperation.getCollectionName());

        try {
            output.put("collectionCreated", mongoDbUtility.createCollection(createCollectionOperation));

        } catch (MongoExecutionTimeoutException ex) {
            return csUtil.buildApiExceptionError(
                    "Max Processing Time Exceeded",
                    ex.getMessage());
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