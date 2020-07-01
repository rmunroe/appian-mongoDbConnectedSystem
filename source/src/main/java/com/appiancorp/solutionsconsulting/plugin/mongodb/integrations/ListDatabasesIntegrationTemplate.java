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
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbUtility;
import com.appiancorp.solutionsconsulting.plugin.mongodb.PropertyDescriptorsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.OUTPUT_TYPE;
import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_ARRAY;


@TemplateId(name = "ListDatabasesIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class ListDatabasesIntegrationTemplate extends SimpleIntegrationTemplate {


    @Override
    protected SimpleConfiguration getConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {

        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        List<PropertyDescriptor<?>> propertyDescriptors = new ArrayList<>();
        PropertyDescriptorsUtil propertyDescriptorsUtil = new PropertyDescriptorsUtil(this, integrationConfiguration, mongoDbUtility, propertyDescriptors);

        propertyDescriptorsUtil.buildOutputTypeProperty();

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }

    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext) {

        ConnectedSystemUtil csUtil = new ConnectedSystemUtil("MongoClient.listDatabaseNames()");
        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);

        String outputType = integrationConfiguration.getValue(OUTPUT_TYPE);

        csUtil.addAllRequestDiagnostic(getRequestDiagnostic(outputType));

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        if (outputType != null && outputType.equals(OUTPUT_TYPE_JSON_ARRAY)) {
            output.put("databases", mongoDbUtility.listDatabasesJson());
        } else {
            output.put("databases", mongoDbUtility.listDatabases());
        }

        csUtil.stopTiming();

        csUtil.addAllResponse(output);

        return csUtil.buildSuccess();
    }

    private Map<String, Object> getRequestDiagnostic(String outputType) {
        Map<String, Object> diagnostic = new HashMap<>();
        diagnostic.put("Output Type", outputType);
        return diagnostic;
    }
}
