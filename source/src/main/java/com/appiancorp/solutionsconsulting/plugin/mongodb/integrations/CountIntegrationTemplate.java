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
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.CollectionCountOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;


/**
 * The CountIntegrationTemplate class is an integration template used to count
 * the number of documents in a specified collection within a MongoDB database.
 * It extends the SimpleIntegrationTemplate to leverage integration capabilities
 * such as configuration and execution processing.
 *
 * <p>
 * This template allows users to specify database and collection details, along
 * with optional parameters such as a filter for query criteria, collation settings,
 * read preferences, and read concerns during the count operation.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
@TemplateId(name = "CountIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class CountIntegrationTemplate extends SimpleIntegrationTemplate {
    /**
     * Configures and returns a SimpleConfiguration object for the integration by building properties
     * based on the provided configurations and context.
     *
     * @param integrationConfiguration the integration configuration provided by the system
     * @param connectedSystemConfiguration the configuration of the connected system
     * @param propertyPath the property path related to the integration's properties
     * @param executionContext the context in which the integration is being executed
     * @return a configured SimpleConfiguration with the appropriate properties set
     */
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

    /**
     * Executes the CountIntegrationTemplate operation, performing a collection count based on the provided
     * configuration and context.
     *
     * @param integrationConfiguration the configuration for the integration containing details like database name, collection name, filters, and read options.
     * @param connectedSystemConfiguration the configuration details for the connected system such as authentication and connection information.
     * @param executionContext the context of the execution, providing information such as logging and diagnostic utilities.
     * @return an IntegrationResponse indicating the result of the execution, including success or error state with additional details.
     */
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
        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.addAllRequestDiagnostic(op.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", op.getDatabaseName());
        output.put("collection", op.getCollectionName());

        try {
            output.put("count", mongoDbUtility.count(op));

        } catch (Exception e) {
            return csUtil.buildApiExceptionError(e);
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }
}
