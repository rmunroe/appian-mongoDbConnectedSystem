package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.mongodb.client.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class IntegrationUtil {
    SimpleConfiguration integrationConfiguration;
    ExecutionContext executionContext;

    public IntegrationUtil(SimpleConfiguration integrationConfiguration, ExecutionContext executionContext) {
        this.integrationConfiguration = integrationConfiguration;
        this.executionContext = executionContext;
    }


    public Collation buildCollation() {
        Map<String, PropertyState> collation = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.COLLATION);

        if (collation != null) {
            Object locale = collation.get(MongoDbConnectedSystemConstants.COLLATION_LOCALE).getValue();
            Boolean caseLevel = (Boolean) collation.get(MongoDbConnectedSystemConstants.COLLATION_CASE_LEVEL).getValue();
            Object caseFirst = collation.get(MongoDbConnectedSystemConstants.COLLATION_CASE_FIRST).getValue();
            Object strength = collation.get(MongoDbConnectedSystemConstants.COLLATION_STRENGTH).getValue();
            Boolean numOrder = (Boolean) collation.get(MongoDbConnectedSystemConstants.COLLATION_NUMERIC_ORDERING).getValue();
            Object alternate = collation.get(MongoDbConnectedSystemConstants.COLLATION_ALTERNATE).getValue();
            Object maxVar = collation.get(MongoDbConnectedSystemConstants.COLLATION_MAX_VARIABLE).getValue();
            Boolean backwards = (Boolean) collation.get(MongoDbConnectedSystemConstants.COLLATION_BACKWARDS).getValue();

            Collation.Builder builder = Collation.builder();

            if (locale != null) builder.locale((String) locale);
            if (caseLevel) builder.caseLevel(true);
            if (caseFirst != null) builder.collationCaseFirst((CollationCaseFirst) caseFirst);
            if (strength != null) builder.collationStrength((CollationStrength) strength);
            if (numOrder) builder.numericOrdering(true);
            if (alternate != null) builder.collationAlternate((CollationAlternate) alternate);
            if (maxVar != null) builder.collationMaxVariable((CollationMaxVariable) maxVar);
            if (backwards) builder.backwards(true);

            return builder.build();
        } else {
            return null;
        }
    }

    public Document writeJsonListToDocument(List<String> jsonList) {
        Boolean asArray = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_FILE_ARRAY);
        Long folderId = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.OUTPUT_FOLDER_ID);
        String fileName = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.OUTPUT_FILE_NAME);
        String charset = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_FILE_CHARSET);

        String outputText;
        if (asArray) {
            outputText = "[" + String.join(",", jsonList) + "]";
        } else {
            outputText = String.join("\n", jsonList);
        }

        InputStream targetStream = new ByteArrayInputStream(outputText.getBytes(Charset.forName(charset)));

        return executionContext.getDocumentDownloadService().downloadDocument(targetStream, folderId, fileName);
    }
}
