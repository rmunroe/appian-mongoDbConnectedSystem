package com.appiancorp.solutionsconsulting.cs.mongodb;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.mongodb.client.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;

public class IntegrationUtil {
    SimpleConfiguration integrationConfiguration;
    ExecutionContext executionContext;

    public IntegrationUtil(SimpleConfiguration integrationConfiguration, ExecutionContext executionContext) {
        this.integrationConfiguration = integrationConfiguration;
        this.executionContext = executionContext;
    }


    public Collation buildCollation() {
        LinkedHashMap<String, PropertyState> collation = integrationConfiguration.getValue(COLLATION);

        if (collation != null) {
            Object locale = collation.get(COLLATION_LOCALE).getValue();
            Boolean caseLevel = (Boolean) collation.get(COLLATION_CASE_LEVEL).getValue();
            Object caseFirst = collation.get(COLLATION_CASE_FIRST).getValue();
            Object strength = collation.get(COLLATION_STRENGTH).getValue();
            Boolean numOrder = (Boolean) collation.get(COLLATION_NUMERIC_ORDERING).getValue();
            Object alternate = collation.get(COLLATION_ALTERNATE).getValue();
            Object maxVar = collation.get(COLLATION_MAX_VARIABLE).getValue();
            Boolean backwards = (Boolean) collation.get(COLLATION_BACKWARDS).getValue();

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

    public Document writeJsonsToDocument(List<String> jsons) {
        Boolean asArray = integrationConfiguration.getValue(OUTPUT_TYPE_JSON_FILE_ARRAY);
        Long folderId = integrationConfiguration.getValue(OUTPUT_FOLDER_ID);
        String fileName = integrationConfiguration.getValue(OUTPUT_FILE_NAME);
        String charset = integrationConfiguration.getValue(OUTPUT_TYPE_JSON_FILE_CHARSET);

        String outputText;
        if (asArray) {
            outputText = "[" + String.join(",", jsons) + "]";
        } else {
            outputText = String.join("\n", jsons);
        }

        InputStream targetStream = new ByteArrayInputStream(outputText.getBytes(Charset.forName(charset)));

        return executionContext.getDocumentDownloadService().downloadDocument(targetStream, folderId, fileName);
    }
}
