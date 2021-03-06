package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.plugin.mongodb.ConnectedSystemUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.IntegrationUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbUtility;
import com.appiancorp.solutionsconsulting.plugin.mongodb.PropertyDescriptorsUtil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Extended SimpleIntegrationTemplate with some extras
 */
public abstract class MongoDbIntegrationTemplate extends SimpleIntegrationTemplate {
    protected MongoDbUtility mongoDbUtility;
    protected List<PropertyDescriptor<?>> propertyDescriptors;
    protected PropertyDescriptorsUtil propertyDescriptorsUtil;
    protected ConnectedSystemUtil csUtil;
    protected IntegrationUtil integrationUtil;


    /**
     * Performs functions common to all getConfiguration() methods
     *
     * @param integrationConfiguration     the injected SimpleConfiguration integrationConfiguration
     * @param connectedSystemConfiguration the injected SimpleConfiguration connectedSystemConfiguration
     * @param propertyPath                 the injected PropertyPath propertyPath
     * @param executionContext             the injected ExecutionContext executionContext
     */
    protected void setupConfiguration(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            PropertyPath propertyPath,
            ExecutionContext executionContext) {

        mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        propertyDescriptors = new ArrayList<>();
        propertyDescriptorsUtil = new PropertyDescriptorsUtil(this, integrationConfiguration, mongoDbUtility, propertyDescriptors);
    }


    /**
     * Performs functions common to all execute() methods
     *
     * @param apiMethodName                the name of the API method the Integration is based on
     * @param integrationConfiguration     the injected SimpleConfiguration integrationConfiguration
     * @param connectedSystemConfiguration the injected SimpleConfiguration connectedSystemConfiguration
     * @param executionContext             the injected ExecutionContext executionContext
     */
    protected void setupExecute(String apiMethodName,
                                SimpleConfiguration integrationConfiguration,
                                SimpleConfiguration connectedSystemConfiguration,
                                ExecutionContext executionContext) {

        csUtil = new ConnectedSystemUtil(apiMethodName);
        mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        integrationUtil = new IntegrationUtil(integrationConfiguration, executionContext);
    }


    /**
     * Determines if the Integration Template has been annotated with IntegrationTemplateRequestPolicy.WRITE (affects change / has side effects)
     *
     * @return true if annotated with IntegrationTemplateRequestPolicy.WRITE
     */
    protected Boolean isWriteOperation() {
        for (Annotation a : this.getClass().getDeclaredAnnotations())
            if (a instanceof IntegrationTemplateType && ((IntegrationTemplateType) a).value().equals(IntegrationTemplateRequestPolicy.WRITE))
                return true;
        return false;
    }


    /**
     * Determines if the Integration Template has been annotated with ModifyOne
     *
     * @return true if annotated with ModifyOne
     */
    protected Boolean isModifyOne() {
        for (Annotation a : this.getClass().getDeclaredAnnotations())
            if (a instanceof ModifyOne)
                return true;
        return false;
    }


    /**
     * Determines if the Integration Template has been annotated with ModifyMany
     *
     * @return true if annotated with ModifyMany
     */
    protected Boolean isModifyMany() {
        for (Annotation a : this.getClass().getDeclaredAnnotations())
            if (a instanceof ModifyMany)
                return true;
        return false;
    }
}
