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

/**
 * Provides utility methods for building a MongoDB {@link Collation} object and
 * creating Appian {@link Document} objects from JSON output.
 * <p>
 * This class primarily uses configuration values from a {@link SimpleConfiguration}
 * to assemble various MongoDB collation settings (locale, case sensitivity,
 * numeric ordering, etc.). It also facilitates writing a list of JSON strings to a
 * downloadable Appian document, leveraging the provided {@link ExecutionContext}.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <ul>
 *   <li>Instantiate this class by passing in a {@link SimpleConfiguration}
 *       and an {@link ExecutionContext}.</li>
 *   <li>Call {@link #buildCollation()} to construct a custom {@link Collation}
 *       using the configured parameters.</li>
 *   <li>Call {@link #writeJsonListToDocument(List)} to return an Appian
 *       {@link Document} containing JSON data.</li>
 * </ul>
 *
 * <p><b>Note:</b> This class assumes the relevant configuration properties
 * (e.g. collation fields, folder ID, etc.) exist in the
 * {@link SimpleConfiguration} passed in at runtime.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class IntegrationUtil {
    SimpleConfiguration integrationConfiguration;
    ExecutionContext executionContext;

    /**
     * Creates a new {@code IntegrationUtil} instance with a given
     * {@link SimpleConfiguration} and {@link ExecutionContext}.
     *
     * @param integrationConfiguration the configuration holding MongoDB collation
     *                                 and output file settings
     * @param executionContext         the context used to download or
     *                                 create documents in Appian
     */
    public IntegrationUtil(SimpleConfiguration integrationConfiguration, ExecutionContext executionContext) {
        this.integrationConfiguration = integrationConfiguration;
        this.executionContext = executionContext;
    }

    /**
     * Builds a MongoDB {@link Collation} object from the settings found in
     * {@code integrationConfiguration}. These settings may include:
     * <ul>
     *   <li>locale (e.g., "en_US")</li>
     *   <li>case level and case first handling</li>
     *   <li>strength (for comparisons like accent, case)</li>
     *   <li>numeric ordering</li>
     *   <li>alternate handling and max variable</li>
     *   <li>backwards accent ordering</li>
     * </ul>
     *
     * @return a new {@link Collation} configured with the specified parameters,
     *         or {@code null} if no collation properties are specified
     */
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

    /**
     * Writes a list of JSON strings to an Appian {@link Document}. If the
     * {@code OUTPUT_TYPE_JSON_FILE_ARRAY} property is set to {@code true},
     * the JSON strings are combined into a JSON array; otherwise, they’re
     * appended line-by-line. The resulting file is then saved to the folder
     * indicated by {@code OUTPUT_FOLDER_ID} using the given file name and
     * character set.
     *
     * @param jsonList a list of JSON strings to be written to the document
     * @return the newly created {@link Document}
     */
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