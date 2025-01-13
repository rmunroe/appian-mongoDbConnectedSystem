package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.util.Map;


/**
 * Represents an operation to create an index on a specified MongoDB collection.
 *
 * <p>
 * The {@code CreateIndexOperation} class extends {@code CollectionWriteOperation} and allows
 * for defining the index to be created using a JSON string representation. This class provides
 * functionality for parsing the JSON string into a {@code Document} and for retrieving diagnostic
 * information about the operation.
 * </p>
 *
 * <p>
 * Instances of this class require a database name, collection name, and a valid JSON representation
 * of the index to be created. If the provided JSON is invalid, an {@code InvalidJsonException} is thrown.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class CreateIndexOperation extends CollectionWriteOperation {
    private String indexJson;
    private Document indexDocument;


    /**
     * Constructs a {@code CreateIndexOperation} to create an index on a specified MongoDB collection.
     * This constructor initializes the operation with the provided database name, collection name,
     * and a JSON string representation of the index specification.
     *
     * <p>
     * The JSON string representation of the index is validated and parsed into a {@code Document}.
     * If the provided JSON is invalid or cannot be parsed, an {@code InvalidJsonException} is thrown.
     * </p>
     *
     * @param databaseName       the name of the MongoDB database where the index will be created
     * @param validateDatabase   whether the database should be validated prior to creating the index
     * @param collectionName     the name of the MongoDB collection where the index will be created
     * @param validateCollection whether the collection should be validated prior to creating the index
     * @param indexJson          the JSON string representation of the index to be created
     * @throws InvalidJsonException if the provided JSON string for the index is invalid or cannot be parsed
     */
    public CreateIndexOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String indexJson
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setIndexJson(indexJson);

        try {
            setIndexDocument(Document.parse(indexJson));
        } catch (Exception ex) {
            throw new InvalidJsonException(
                    "Index JSON: Invalid JSON provided.",
                    indexJson);
        }
    }


    /**
     * Retrieves diagnostic information specific to the index creation operation.
     * This method enhances the diagnostic data by including the JSON representation
     * of the index, in addition to the data provided by the base operation.
     *
     * @return a map of key-value pairs where the keys are diagnostic field names
     *         and the values are their corresponding data, including the index JSON.
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Index JSON", getIndexJson());
        return diagnostic;
    }

    /**
     * Retrieves the JSON string representation of the index to be created.
     *
     * @return a JSON string representing the index configuration.
     */
    public String getIndexJson() {
        return indexJson;
    }

    /**
     * Sets the JSON string representation of the index to be created.
     *
     * @param indexJson the JSON string defining the index. This parameter should contain
     *                  a valid JSON representation of the index definition that adheres
     *                  to MongoDB's indexing structure and syntax.
     */
    public void setIndexJson(String indexJson) {
        this.indexJson = indexJson;
    }

    /**
     * Retrieves the index document associated with this operation.
     *
     * <p>
     * The index document represents the parsed JSON definition of the index
     * that is to be created on a MongoDB collection. It is generated from the
     * provided JSON string during the initialization of the operation.
     * </p>
     *
     * @return a Document object representing the index to be created
     */
    public Document getIndexDocument() {
        return indexDocument;
    }

    /**
     * Sets the index document that defines the structure and properties of an index in MongoDB.
     *
     * @param indexDocument the {@code Document} object representing the index to be created
     */
    public void setIndexDocument(Document indexDocument) {
        this.indexDocument = indexDocument;
    }
}
