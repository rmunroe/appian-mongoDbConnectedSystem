package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;

/**
 * Represents an operation for inserting a single document into a MongoDB collection.
 * This class extends {@code CollectionWriteOperation}, inheriting functionality
 * for targeting a specific database and collection while adding behaviors specific
 * to single document insertion operations.
 *
 * <p>
 * The {@code InsertOneOperation} class provides functionality to parse and validate
 * a JSON string that represents the document to be inserted. It also supports
 * configurations for skipping DateTime conversion during the document's preparation process.
 *</p>
 *
 * <p>
 * This operation is typically used to insert a single document in MongoDB while
 * ensuring that the provided JSON data is valid.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class InsertOneOperation extends CollectionWriteOperation {
    private String outputType;
    private String json;
    private Boolean skipDateTimeConversion;
    private Document document;

    /**
     * Constructs an {@code InsertOneOperation} object that performs a single document
     * insertion into the specified MongoDB collection. The JSON representation of the
     * document is parsed, validated, and prepared for insertion. This operation also supports
     * skipping DateTime conversion during preparation if specified.
     *
     * @param databaseName           the name of the database to target
     * @param validateDatabase       whether the database should be validated prior to running the operation
     * @param collectionName         the name of the collection to target
     * @param validateCollection     whether the collection should be validated prior to running the operation
     * @param outputType             the type of output expected from the operation
     * @param json                   the JSON string representing the document to be inserted
     * @param skipDateTimeConversion whether to skip DateTime conversion during document preparation
     * @throws InvalidJsonException  if the provided JSON string is invalid or cannot be parsed
     */
    public InsertOneOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String json, Boolean skipDateTimeConversion
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setJson(json);
        setSkipDateTimeConversion(skipDateTimeConversion);

        try {
            setDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(json), getSkipDateTimeConversion()));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Insert One JSON: Invalid JSON provided.",
                    json);
        }
    }


    /**
     * Retrieves diagnostic information for the InsertOne operation, including specific
     * details about the output type and the JSON representation of the document to be inserted.
     * This method extends the base diagnostic information provided by the parent class.
     *
     * @return a map of key-value pairs containing diagnostic data, including:
     *         "Output Type" - the type of output expected for this operation,
     *         "Insert One JSON" - the JSON string representation of the document to be inserted
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Insert One JSON", getJson());

        return diagnostic;
    }


    /**
     * Retrieves the output type for the operation.
     *
     * @return a String representing the output type of the operation
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the output type for this operation.
     *
     * @param outputType the output type to be set, which determines the format
     *                   or structure of the results produced by the operation
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * Retrieves the JSON representation of the data relevant to the operation.
     *
     * @return the JSON string associated with the operation
     */
    public String getJson() {
        return json;
    }

    /**
     * Sets the JSON string representation of the document to be used in the operation.
     *
     * @param json a JSON string that represents the document. This string will be parsed
     *             and validated for further use within the context of the operation.
     */
    public void setJson(String json) {
        this.json = json;
    }

    /**
     * Retrieves the document prepared for insertion into the MongoDB collection.
     *
     * @return the {@link Document} instance to be inserted. This document is parsed and prepared
     *         based on the JSON input provided to the operation and may reflect any preprocessing
     *         such as skipping DateTime conversion or applying validation rules.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Sets the document to be inserted into a MongoDB collection.
     *
     * @param document the {@link Document} object representing the MongoDB document to be set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * Retrieves the flag indicating whether DateTime conversion should be skipped
     * during the preparation of the document for insertion.
     *
     * @return a {@code Boolean} indicating if DateTime conversion is skipped.
     */
    public Boolean getSkipDateTimeConversion() {
        return skipDateTimeConversion;
    }

    /**
     * Sets whether DateTime conversion should be skipped during the preparation
     * process of the document to be inserted.
     *
     * @param skipDateTimeConversion a {@code Boolean} value indicating whether
     *                               DateTime conversion should be skipped.
     *                               If {@code true}, the document's DateTime
     *                               fields will not be converted to the default format.
     */
    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }
}
