package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.DropCollectionOperation;
import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;

import java.util.HashMap;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;


@TemplateId(name = "DropCollectionIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class DropCollectionIntegrationTemplate extends MongoDbIntegrationTemplate {
    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext
    ) {
        this.setupConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);

        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }


    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext
    ) {
        this.setupExecute("MongoCollection.drop()", integrationConfiguration, connectedSystemConfiguration, executionContext);

        DropCollectionOperation dropCollectionOperation = new DropCollectionOperation(
                integrationConfiguration.getValue(DATABASE),
                integrationConfiguration.getValue(DATABASE_EXISTS),
                integrationConfiguration.getValue(COLLECTION),
                integrationConfiguration.getValue(COLLECTION_EXISTS)
        );

        csUtil.addAllRequestDiagnostic(dropCollectionOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", dropCollectionOperation.getDatabaseName());
        output.put("collection", dropCollectionOperation.getCollectionName());

        try {
            output.put("collectionDropped", mongoDbUtility.dropCollection(dropCollectionOperation));

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