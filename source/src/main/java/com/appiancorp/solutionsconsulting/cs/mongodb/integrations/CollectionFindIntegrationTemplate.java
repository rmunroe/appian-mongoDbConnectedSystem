package com.appiancorp.solutionsconsulting.cs.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.cs.mongodb.ConnectedSystemUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingCollectionException;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingDatabaseException;
import com.appiancorp.solutionsconsulting.cs.mongodb.IntegrationUtil;
import com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbUtility;
import com.appiancorp.solutionsconsulting.cs.mongodb.Operations.CollectionFindOperation;
import com.appiancorp.solutionsconsulting.cs.mongodb.PropertyDescriptorsUtil;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoQueryException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;


@TemplateId(name = "CollectionFindIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class CollectionFindIntegrationTemplate extends SimpleIntegrationTemplate {

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
        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        Object collectionName = integrationConfiguration.getValue(COLLECTION);
        if (collectionName != null) {
            propertyDescriptorsUtil.buildCollectionFindProperties();
        }

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }

    @Override
    protected IntegrationResponse execute(
            SimpleConfiguration integrationConfiguration,
            SimpleConfiguration connectedSystemConfiguration,
            ExecutionContext executionContext) {

        ConnectedSystemUtil csUtil = new ConnectedSystemUtil("MongoCollection.find()");
        MongoDbUtility mongoDbUtility = new MongoDbUtility(connectedSystemConfiguration);
        IntegrationUtil integrationUtil = new IntegrationUtil(integrationConfiguration, executionContext);

        CollectionFindOperation findOperation;
        try {
            findOperation = new CollectionFindOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),
                    integrationConfiguration.getValue(READ_PREFERENCE),
                    integrationConfiguration.getValue(READ_CONCERN),

                    integrationConfiguration.getValue(OUTPUT_TYPE),
                    integrationConfiguration.getValue(FILTER_JSON),
                    integrationConfiguration.getValue(SORT_JSON),
                    integrationConfiguration.getValue(PROJECTION_JSON),

                    integrationConfiguration.getValue(LIMIT) != null ? integrationConfiguration.getValue(LIMIT) : 0,
                    integrationConfiguration.getValue(SKIP) != null ? integrationConfiguration.getValue(SKIP) : 0,
                    integrationConfiguration.getValue(RECORD_ID),
                    integrationConfiguration.getValue(MAX_TIME),

                    integrationUtil.buildCollation()
            );
        } catch (InvalidJsonException e) {
            return csUtil.buildApiExceptionError(
                    e.getMessage(),
                    "Invalid JSON string: \"" + e.jsonString + "\"");
        }

        csUtil.addAllRequestDiagnostic(findOperation.getRequestDiagnostic());

        Map<String, Object> output = new HashMap<>();

        csUtil.startTiming();

        output.put("database", findOperation.getDatabaseName());
        output.put("collection", findOperation.getCollectionName());

        try {
            if (findOperation.getOutputType() != null && findOperation.getOutputType().equals(OUTPUT_TYPE_JSON_ARRAY)) {
                output.put("documents", mongoDbUtility.findJson(findOperation));
            } else if (findOperation.getOutputType() != null && findOperation.getOutputType().equals(OUTPUT_TYPE_JSON_FILE)) {
                Boolean asArray = integrationConfiguration.getValue(OUTPUT_TYPE_JSON_FILE_ARRAY);
                Long folderId = integrationConfiguration.getValue(OUTPUT_FOLDER_ID);
                String fileName = integrationConfiguration.getValue(OUTPUT_FILE_NAME);
                String charset = integrationConfiguration.getValue(OUTPUT_TYPE_JSON_FILE_CHARSET);

                List<String> jsonList = mongoDbUtility.findJson(findOperation);
                String outputText;
                if (asArray) {
                    outputText = "[" + String.join(",", jsonList) + "]";
                } else {
                    outputText = String.join("\n", jsonList);
                }

                InputStream targetStream = new ByteArrayInputStream(outputText.getBytes(Charset.forName(charset)));

                Document document = executionContext.getDocumentDownloadService()
                        .downloadDocument(targetStream, folderId, fileName);

                output.put("jsonDocument", document);

            } else {
                output.put("documents", mongoDbUtility.find(findOperation));
            }
        } catch (MongoExecutionTimeoutException ex) {
            return csUtil.buildApiExceptionError(
                    "Max Processing Time Exceeded",
                    ex.getMessage());
        } catch (MongoQueryException ex) {
            return csUtil.buildApiExceptionError(
                    "Query Exception",
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
