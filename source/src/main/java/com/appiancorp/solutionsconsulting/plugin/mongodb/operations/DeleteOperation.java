package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.mongodb.client.model.Collation;
import org.bson.Document;

import java.util.Map;


/**
 * Represents a delete operation performed on a MongoDB collection.
 * This class extends {@code CollectionWriteOperation} and provides specific
 * functionalities to define and execute delete instructions, including filters
 * and collation settings.
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class DeleteOperation extends CollectionWriteOperation {
    private String outputType;
    private String filterJson;
    private Collation collation;

    private Document filterDocument;

    /**
     * Constructs a {@code DeleteOperation} that defines a delete action on a MongoDB collection.
     * The delete operation is configured using the database and collection information,
     * output type, filter criteria in JSON format, and optional collation settings.
     *
     * @param databaseName       the name of the database where the delete operation will be executed
     * @param validateDatabase   specifies whether the database should be validated before execution
     * @param collectionName     the name of the collection where the delete operation will be applied
     * @param validateCollection specifies whether the collection should be validated before execution
     * @param outputType         the output format for the result of the delete operation
     * @param filterJson         the JSON string defining the filter criteria for the delete operation
     * @param collation          an optional {@code Collation} object to specify collation settings
     *                            for string comparison during the delete operation
     * @throws InvalidJsonException if the provided filter JSON is invalid or cannot be parsed
     */
    public DeleteOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String filterJson, Collation collation
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setFilterJson(filterJson);
        setCollation(collation);

        try {
            setFilterDocument(MongoDocumentUtil.prepDocumentForInsert(Document.parse(getFilterJson()), false));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Delete JSON: Invalid JSON provided.",
                    getFilterJson());
        }
    }


    /**
     * Retrieves a diagnostic map with additional details specific to the delete operation.
     * This method enhances the diagnostic information by including operation-specific
     * parameters such as the output type and filter JSON.
     *
     * @return a map containing diagnostic information where keys are parameter names
     *         and values are their corresponding details, including inherited
     *         diagnostics and operation-specific data.
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Filter JSON", getFilterJson());

        return diagnostic;
    }

    /**
     * Retrieves the output type associated with the delete operation.
     *
     * @return a {@code String} representing the output type of the operation
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the output type for the delete operation.
     *
     * @param outputType the type of output to be used for the operation
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * Retrieves the JSON representation of the filter criteria for the delete operation.
     *
     * @return a String representing the JSON filter used to define the documents to be deleted
     */
    public String getFilterJson() {
        return filterJson;
    }

    /**
     * Sets the filter criteria for the delete operation in JSON format.
     * The filter JSON defines the conditions used to identify documents
     * to be deleted from the MongoDB collection.
     *
     * @param filterJson the filter criteria in JSON format
     */
    public void setFilterJson(String filterJson) {
        this.filterJson = filterJson;
    }

    /**
     * Retrieves the filter document that specifies the criteria for the delete operation.
     *
     * @return a {@link Document} representing the filter criteria used in the delete operation
     */
    public Document getFilterDocument() {
        return filterDocument;
    }

    /**
     * Sets the filter document for the delete operation. The filter document is
     * used to specify the conditions for identifying the documents to be deleted
     * from the MongoDB collection.
     *
     * @param filterDocument a {@link Document} representing the filter criteria to
     *                        identify the target documents for deletion.
     */
    public void setFilterDocument(Document filterDocument) {
        this.filterDocument = filterDocument;
    }

    /**
     * Retrieves the collation settings associated with the delete operation.
     * Collation allows specifying language-specific rules for string comparison,
     * such as case sensitivity and accent marks, enabling fine-grained control
     * over queries and operations on text data.
     *
     * @return the {@link Collation} object representing the collation settings,
     *         or null if no collation has been defined.
     */
    public Collation getCollation() {
        return collation;
    }

    /**
     * Sets the collation for the delete operation.
     * Collation allows specifying language-specific rules for string comparison,
     * such as case and accent sensitivity, to tailor query execution.
     *
     * @param collation the collation object specifying collation options to be applied
     */
    public void setCollation(Collation collation) {
        this.collation = collation;
    }
}
