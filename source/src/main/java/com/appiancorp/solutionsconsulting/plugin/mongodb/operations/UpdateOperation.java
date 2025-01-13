package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;

/**
 * Represents an update operation to be performed on a MongoDB collection.
 *
 * <p>
 * This class extends {@code CollectionWriteOperation} to provide specific behaviors for
 * constructing and executing update operations. An update operation requires a filter
 * to identify the documents to be updated and update instructions to specify the changes
 * to those documents. Additionally, it supports options like output type and skip date-time
 * conversion for enhanced flexibility.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class UpdateOperation extends CollectionWriteOperation {
    private String outputType;
    private String filterJson;
    private String updateJson;
    private Boolean skipDateTimeConversion;
    private Document filterDocument;
    private Document updateDocument;

    /**
     * Constructs an UpdateOperation object with specified parameters for targeting
     * a MongoDB collection and performing update operations. The provided filter
     * and update JSON strings are parsed into MongoDB documents and validated for
     * correct format.
     *
     * @param databaseName            the name of the database to target
     * @param validateDatabase        whether the database name should be validated
     * @param collectionName          the name of the collection to target
     * @param validateCollection      whether the collection name should be validated
     * @param outputType              the type of output expected from the operation
     * @param filterJson              the JSON string defining the filter criteria for the operation
     * @param updateJson              the JSON string defining the update instructions for the operation
     * @param skipDateTimeConversion  whether datetime values should bypass conversion during parsing
     * @throws InvalidJsonException   if the filterJson or updateJson strings are invalid
     */
    public UpdateOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String filterJson, String updateJson, Boolean skipDateTimeConversion
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setFilterJson(filterJson);
        setUpdateJson(updateJson);
        setSkipDateTimeConversion(skipDateTimeConversion);

        try {
            setFilterDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(getFilterJson()), getSkipDateTimeConversion()));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Update JSON: Invalid Filter JSON provided.",
                    getFilterJson());
        }
        try {
            setUpdateDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(getUpdateJson()), getSkipDateTimeConversion()));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Update JSON: Invalid Update Instructions JSON provided.",
                    getUpdateJson());
        }
    }


    /**
     * Retrieves diagnostic information for the current update operation.
     * This method extends the diagnostic data provided by the parent class by including
     * specific details relevant to the update operation, such as:
     * - Output type of the operation.
     * - JSON representation of the filter criteria.
     * - JSON representation of the update instructions.
     * - A flag indicating whether to skip date-time conversion.
     *
     * @return a map containing diagnostic data where the keys are descriptive diagnostic
     *         labels and the values are the associated data for this update operation.
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Filter JSON", getFilterJson());
        diagnostic.put("Update JSON", getUpdateJson());
        diagnostic.put("Skip Date Time Conversion", getSkipDateTimeConversion());

        return diagnostic;
    }


    /**
     * Retrieves the output type associated with the operation.
     *
     * @return the output type as a String
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the output type for the operation.
     *
     * @param outputType the desired output type to be set for the operation
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * Retrieves the filter JSON string used to define the query conditions.
     *
     * @return a String representing the filter JSON
     */
    public String getFilterJson() {
        return filterJson;
    }

    /**
     * Sets the filter JSON string used to specify the filter criteria
     * for MongoDB operations.
     *
     * @param filterJson the JSON string representing the filter criteria
     */
    public void setFilterJson(String filterJson) {
        this.filterJson = filterJson;
    }

    /**
     * Retrieves the filter document associated with this operation.
     *
     * @return a {@link Document} representing the filter criteria for the operation
     */
    public Document getFilterDocument() {
        return filterDocument;
    }

    /**
     * Sets the filter document to be used in the MongoDB operation.
     *
     * @param filterDocument the filter document of type {@link Document} used to specify the criteria
     *                       for selecting the documents in the MongoDB collection
     */
    public void setFilterDocument(Document filterDocument) {
        this.filterDocument = filterDocument;
    }

    /**
     * Retrieves the skipDateTimeConversion flag.
     *
     * <p>
     * This method is used to determine whether date-time conversion should
     * be skipped during the execution of the operation. The return value
     * indicates the current state of this configuration.
     * </p>
     *
     * @return {@code true} if date-time conversion should be skipped, {@code false} otherwise
     */
    public Boolean getSkipDateTimeConversion() {
        return skipDateTimeConversion;
    }

    /**
     * Sets the skipDateTimeConversion flag, determining whether date and time
     * conversion should be skipped during the operation.
     *
     * @param skipDateTimeConversion a Boolean value indicating whether to skip
     *                               the date and time conversion. If true,
     *                               the conversion will be bypassed.
     */
    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }

    /**
     * Retrieves the JSON string representing the update operation.
     *
     * @return a {@code String} containing the JSON representation of the update.
     */
    public String getUpdateJson() {
        return updateJson;
    }

    /**
     * Sets the JSON string representation of the update operation.
     *
     * @param updateJson the JSON string that specifies the update operation
     */
    public void setUpdateJson(String updateJson) {
        this.updateJson = updateJson;
    }

    /**
     * Retrieves the update document for the operation.
     *
     * @return the Document representing the update criteria or data to be applied in the operation
     */
    public Document getUpdateDocument() {
        return updateDocument;
    }

    /**
     * Sets the update document to be used in the update operation.
     *
     * @param updateDocument the {@link Document} representing the update to be applied.
     */
    public void setUpdateDocument(Document updateDocument) {
        this.updateDocument = updateDocument;
    }
}
