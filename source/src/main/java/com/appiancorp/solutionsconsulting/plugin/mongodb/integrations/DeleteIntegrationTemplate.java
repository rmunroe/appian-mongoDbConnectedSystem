package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.DeleteOperation;

import java.util.HashMap;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;


@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class DeleteIntegrationTemplate extends MongoDbIntegrationTemplate {
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
            propertyDescriptorsUtil.buildFilterJsonProperty(true);
            propertyDescriptorsUtil.buildCollationsProperty();
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
        if (this.isModifyOne())
            apiMethodName = "MongoCollection.updateOne()";
        else
            apiMethodName = "MongoCollection.updateMany()";

        this.setupExecute(apiMethodName, integrationConfiguration, connectedSystemConfiguration, executionContext);

        DeleteOperation deleteOperation;
        try {
            deleteOperation = new DeleteOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),

                    integrationConfiguration.getValue(OUTPUT_TYPE),
                    integrationConfiguration.getValue(FILTER_JSON),

                    integrationUtil.buildCollation()
            );
        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.addAllRequestDiagnostic(deleteOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", deleteOperation.getDatabaseName());
        output.put("collection", deleteOperation.getCollectionName());

        try {
            if (this.isModifyOne())
                output.put("deleteResult", mongoDbUtility.deleteOne(deleteOperation));
            else
                output.put("deleteResult", mongoDbUtility.deleteMany(deleteOperation));


        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }
}