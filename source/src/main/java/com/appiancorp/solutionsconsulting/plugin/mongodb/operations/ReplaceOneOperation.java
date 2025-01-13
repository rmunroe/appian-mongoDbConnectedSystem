package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;

/**
 * Represents an operation that performs a single document replacement in a MongoDB collection.
 * This operation replaces a document matching a specified filter with a provided replacement document.
 *
 * <p>
 * The operation supports validation of the database and collection prior to execution and provides
 * mechanisms for preparing MongoDB documents and parsing JSON strings. If the filter or replacement
 * JSON is invalid, an {@code InvalidJsonException} is thrown.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class ReplaceOneOperation extends CollectionWriteOperation {
    private String outputType;
    private String filterJson;
    private String replacementJson;
    private Boolean skipDateTimeConversion;
    private Document filterDocument;
    private Document replacementDocument;

    /**
     * Creates a new instance of the ReplaceOneOperation, which represents a single replace operation
     * to be executed on a MongoDB collection. This operation uses the specified filter JSON to locate
     * the document to replace and the replacement JSON to provide the new document.
     *
     * @param databaseName            the name of the database where the operation will be executed
     * @param validateDatabase        specifies whether the database name should be validated
     * @param collectionName          the name of the collection where the operation will be executed
     * @param validateCollection      specifies whether the collection name should be validated
     * @param outputType              the type of output expected from the operation
     * @param filterJson              the JSON string specifying the filter criteria for selecting the document to replace
     * @param replacementJson         the JSON string defining the replacement document
     * @param skipDateTimeConversion  specifies whether to skip conversion of date-time fields during processing
     * @throws InvalidJsonException   if the provided filter JSON or replacement JSON is not valid or cannot be parsed
     */
    public ReplaceOneOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String filterJson, String replacementJson, Boolean skipDateTimeConversion
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setFilterJson(filterJson);
        setReplacementJson(replacementJson);
        setSkipDateTimeConversion(skipDateTimeConversion);

        try {
            setFilterDocument(Document.parse(filterJson));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Replace One JSON: Invalid Filter JSON provided.",
                    filterJson);
        }
        try {
            setReplacementDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(replacementJson), getSkipDateTimeConversion()));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Replace One JSON: Invalid Replacement JSON provided.",
                    replacementJson);
        }
    }


    /**
     * Retrieves diagnostic information related to the replace operation.
     * This method enhances the diagnostic map provided by the parent class
     * by including operation-specific details such as the output type,
     * filter JSON, and replacement JSON.
     *
     * @return a map containing diagnostic key-value pairs, including:
     *         - "Output Type": the type of output expected from the operation
     *         - "Filter JSON": the JSON string used to filter the document to be replaced
     *         - "Replace One JSON": the JSON string representing the replacement document
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Filter JSON", getFilterJson());
        diagnostic.put("Replace One JSON", getReplacementJson());

        return diagnostic;
    }

    /**
     * Retrieves the output type for the current operation.
     * The output type specifies the expected format or type of the operation's outcome.
     *
     * @return a string representing the output type of the operation
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the output type for the operation.
     *
     * @param outputType the type of output expected from the operation
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     *
     */
    public String getFilterJson() {
        return filterJson;
    }

    /**
     * Sets the JSON string used to define the filter criteria for selecting the document.
     *
     * @param filterJson the JSON string representing the filter criteria to locate the target document.
     */
    public void setFilterJson(String filterJson) {
        this.filterJson = filterJson;
    }

    /**
     * Retrieves the JSON string representing the replacement document.
     *
     * @return a String containing the replacement document in JSON format
     */
    public String getReplacementJson() {
        return replacementJson;
    }

    /**
     * Sets the replacement JSON string that defines the replacement document
     * to be used in the replace operation.
     *
     * @param replacementJson the JSON string representing the replacement document
     */
    public void setReplacementJson(String replacementJson) {
        this.replacementJson = replacementJson;
    }

    /**
     * Retrieves the current status of the skipDateTimeConversion flag.
     * This flag determines whether date-time field conversion should be skipped
     * during the replace operation.
     *
     * @return a Boolean indicating if date-time conversion is skipped (true) or not (false)
     */
    public Boolean getSkipDateTimeConversion() {
        return skipDateTimeConversion;
    }

    /**
     * Specifies whether date-time fields should be skipped or converted during processing.
     *
     * @param skipDateTimeConversion a Boolean value indicating whether to skip date-time field conversion.
     */
    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }

    /**
     * Retrieves the filter document used to specify the criteria for selecting
     * the document to be replaced or updated in the MongoDB operation.
     *
     * @return the filter document as an instance of {@code Document}
     */
    public Document getFilterDocument() {
        return filterDocument;
    }

    /**
     * Sets the filter document used to locate the MongoDB document(s)
     * to be modified or replaced during the operation.
     *
     * @param filterDocument the MongoDB {@code Document} object defining
     *                       the criteria for selecting the document(s)
     */
    public void setFilterDocument(Document filterDocument) {
        this.filterDocument = filterDocument;
    }

    /**
     * Retrieves the replacement document associated with the replace operation.
     *
     * @return the replacement document represented as a {@link Document}
     */
    public Document getReplacementDocument() {
        return replacementDocument;
    }

    /**
     * Sets the replacement document for the replace operation.
     *
     * @param replacementDocument the {@link Document} that will replace the original document
     *                            in the target MongoDB collection
     */
    public void setReplacementDocument(Document replacementDocument) {
        this.replacementDocument = replacementDocument;
    }
}