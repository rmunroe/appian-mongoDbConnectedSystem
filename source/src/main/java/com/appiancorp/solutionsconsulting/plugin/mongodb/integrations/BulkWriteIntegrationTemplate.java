package com.appiancorp.solutionsconsulting.plugin.mongodb.integrations;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.*;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemTemplate;
import com.appiancorp.solutionsconsulting.plugin.mongodb.operations.BulkWriteOperation;
import com.mongodb.bulk.BulkWriteResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.*;

//EDITED: bulk-write
@TemplateId(name = "BulkWriteIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class BulkWriteIntegrationTemplate extends MongoDbIntegrationTemplate {
    private static final String API_METHOD_NAME = "MongoCollection.bulkWrite()";
    private static final Logger LOGGER = (Logger) LogManager.getLogger(BulkWriteIntegrationTemplate.class);

    @Override
    protected SimpleConfiguration getConfiguration(SimpleConfiguration integrationConfiguration, SimpleConfiguration connectedSystemConfiguration, PropertyPath propertyPath, ExecutionContext executionContext) {
        //set up configurations
        try {
            this.setupConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);
        } catch (Exception e) {
            LOGGER.error("Failed to setup configurations. "+e.getLocalizedMessage(), e);
        }

        //build properties
        propertyDescriptorsUtil.buildDatabaseProperty();
        propertyDescriptorsUtil.buildCollectionsProperty();

        //update properties
        if (integrationConfiguration.getValue(COLLECTION) != null) {
            propertyDescriptors.add(TextPropertyDescriptor.builder()
                    .key(BULK_WRITE_JSON)
                    .label("Bulk Write Operations JSON")
                    .description("A JSON array of objects representing the operations to be done in the collection. Valid operations are insertOne, deleteOne, updateOne, replaceOne, deleteMany, updateMany.")
                    .instructionText("A JSON array of objects representing the operations to be done in the collection. Valid operations are insertOne, deleteOne, updateOne, replaceOne, deleteMany, updateMany.")
                    .isExpressionable(true)
                    .displayHint(DisplayHint.EXPRESSION)
                    .isRequired(true)
                    .build()
            );
            propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                    .key(BULK_WRITE_IS_ORDERED)
                    .label("Ordered")
                    .description("Select this option to enable ordered execution of operations in the Bulk Write.")
                    .isExpressionable(true)
                    .isRequired(false)
                    .instructionText("Optional. A boolean specifying whether the mongod instance should perform an ordered or unordered operation execution. Defaults to false.")
                    .displayMode(BooleanDisplayMode.RADIO_BUTTON)
                    .build()
            );
            propertyDescriptorsUtil.buildInsertOptionsProperties();
        }

        return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
    }

    @Override
    protected IntegrationResponse execute(SimpleConfiguration integrationConfiguration, SimpleConfiguration connectedSystemConfiguration, ExecutionContext executionContext) {
        //get the values
        String connectionString = (connectedSystemConfiguration.getValue(MongoDbConnectedSystemTemplate.CONNECTION_STRING) == null)? null: connectedSystemConfiguration.getValue(MongoDbConnectedSystemTemplate.CONNECTION_STRING).toString().trim();
        String databaseName = (integrationConfiguration.getValue(DATABASE) == null)? null: integrationConfiguration.getValue(DATABASE).toString().trim();
        String collectionName = (integrationConfiguration.getValue(COLLECTION) == null)? null: integrationConfiguration.getValue(COLLECTION).toString().trim();
        String bulkWriteJson = (integrationConfiguration.getValue(BULK_WRITE_JSON) == null)? null: integrationConfiguration.getValue(BULK_WRITE_JSON).toString().trim();
        try {
            if (StringUtils.isEmpty(connectionString)) throw new Exception("No connection string provided.");
            if (StringUtils.isEmpty(databaseName)) throw new Exception("Invalid value for database. Database cannot be null or empty.");
            if (StringUtils.isEmpty(collectionName)) throw new Exception("Invalid value for collection. Collection cannot be null or empty.");
            if (StringUtils.isEmpty(bulkWriteJson)) throw new Exception("Invalid value for Bulk Write Operations JSON. At least one operation must be provided.");
        } catch (Exception e) {
            LOGGER.error("Invalid configuration. "+e.getLocalizedMessage(), e);
            return csUtil.buildApiExceptionError(e);
        }

        this.setupExecute(API_METHOD_NAME, integrationConfiguration, connectedSystemConfiguration, executionContext);
        BulkWriteOperation bulkWriteOperation = null;
        try {
            bulkWriteOperation = new BulkWriteOperation(
                    integrationConfiguration.getValue(DATABASE),
                    integrationConfiguration.getValue(DATABASE_EXISTS),
                    integrationConfiguration.getValue(COLLECTION),
                    integrationConfiguration.getValue(COLLECTION_EXISTS),
                    integrationConfiguration.getValue(BULK_WRITE_JSON),
                    integrationConfiguration.getValue(BULK_WRITE_IS_ORDERED),
                    integrationConfiguration.getValue(INSERT_SKIP_DATETIME_CONVERSION)
            );
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return csUtil.buildApiExceptionError(e);
        }

        Map<String, Object> output = new HashMap<>();

        //start the timing
        csUtil.startTiming();

        //send the request
        try {
            BulkWriteResult result = mongoDbUtility.bulkWrite(bulkWriteOperation);
            if (result != null) {
                output.put("insertedCount", result.getInsertedCount());
                output.put("deletedCount", result.getDeletedCount());
                output.put("modifiedCount", result.getModifiedCount());
                output.put("wasAcknowledged", result.wasAcknowledged());
                output.put("upsertedCount", result.getUpserts().size());
            }
        }catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return csUtil.buildApiExceptionError(e);
        }

        //stop timing
        csUtil.stopTiming();

        //build response
        csUtil.addAllResponse(output);
        csUtil.addAllRequestDiagnostic(
                new HashMap<String, Object>(){{
                    put("Skip Automatic Date Time Conversion", integrationConfiguration.getValue(INSERT_SKIP_DATETIME_CONVERSION));
                    put("Collection Name", integrationConfiguration.getValue(COLLECTION));
                    put("Operation", API_METHOD_NAME);
                }}
        );

        return csUtil.buildSuccess();
    }

}
