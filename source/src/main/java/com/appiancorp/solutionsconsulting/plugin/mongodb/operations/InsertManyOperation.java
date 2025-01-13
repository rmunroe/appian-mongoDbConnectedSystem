package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDocumentUtil;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbConnectedSystemConstants.INSERT_SOURCE_DOCUMENT;

/**
 * Represents an operation for inserting multiple documents into a MongoDB collection.
 * This operation is based on the {@link CollectionWriteOperation} class and
 * facilitates batch document insertions while adhering to configurable parameters
 * such as validation and JSON array parsing.
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class InsertManyOperation extends CollectionWriteOperation {

    private String outputType;
    private String insertSource;
    private com.appian.connectedsystems.templateframework.sdk.configuration.Document sourceFile;
    private Boolean fileIsArray;
    private String jsonArray;
    private Boolean skipDateTimeConversion;
    private List<Document> documents;

    /**
     * Constructs an InsertManyOperation to insert multiple documents into a MongoDB collection.
     *
     * @param databaseName             the name of the database to target.
     * @param validateDatabase         whether the database should be validated prior to execution.
     * @param collectionName           the name of the collection to target.
     * @param validateCollection       whether the collection should be validated prior to execution.
     * @param outputType               the output type for the result of the operation.
     * @param insertSource             the source from which documents will be inserted (e.g., a file or a string).
     * @param sourceFile               the document file containing the JSON content to be inserted, if applicable.
     * @param fileIsArray              whether the source file contains a JSON array.
     * @param sourceJsonArray          the pre-parsed JSON array as a string to be inserted.
     * @param skipDateTimeConversion   whether to skip converting datetime values in the provided JSON documents.
     * @throws InvalidJsonException    if the provided JSON array is invalid or not properly formatted.
     */
    public InsertManyOperation(
            String databaseName, Boolean validateDatabase,
            String collectionName, Boolean validateCollection,
            String outputType, String insertSource,
            com.appian.connectedsystems.templateframework.sdk.configuration.Document sourceFile, Boolean fileIsArray,
            String sourceJsonArray, Boolean skipDateTimeConversion
    ) throws InvalidJsonException {
        super(databaseName, validateDatabase, collectionName, validateCollection);

        setOutputType(outputType);
        setInsertSource(insertSource);
        setSourceFile(sourceFile);
        setFileIsArray(fileIsArray);

        if (getInsertSource().equals(INSERT_SOURCE_DOCUMENT)) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(sourceFile.getInputStream(), StandardCharsets.UTF_8))) {
                if (getFileIsArray()) {
                    // Read entire file contents into JSON array
                    sourceJsonArray = br.lines().collect(Collectors.joining("\n"));
                } else {
                    // Create a single JSON array from the file contents
                    sourceJsonArray = "[" + br.lines().collect(Collectors.joining(", ")) + "]";
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        setJsonArray(sourceJsonArray);

        setSkipDateTimeConversion(skipDateTimeConversion);

        List<Document> documents = new ArrayList<>();
        if (sourceJsonArray.startsWith("[") && sourceJsonArray.endsWith("]")) {
            try {
                @SuppressWarnings("unchecked")
                List<Document> docs = (List<Document>) Document.parse("{docs:" + sourceJsonArray + "}").get("docs");
                docs.forEach(doc ->
                        documents.add(MongoDocumentUtil.prepDocumentForInsert(doc, getSkipDateTimeConversion()))
                );
            } catch (Exception ex) {
                throw new InvalidJsonException(
                        "Insert Many JSONs: Invalid JSON provided.",
                        sourceJsonArray);
            }
        } else {
            throw new InvalidJsonException(
                    "Insert Many JSONs: JSON provided does not appear to be an ARRAY of Documents.",
                    sourceJsonArray);
        }

        setDocuments(documents);
    }


    /**
     * Retrieves the diagnostic information specific to the "Insert Many" operation.
     * This method builds upon the diagnostics provided by the parent class by adding
     * additional key-value pairs specific to this operation, such as the output type
     * and the JSON array being inserted.
     *
     * @return a map containing diagnostic key-value pairs, including the "Output Type"
     *         and "Insert Many JSON" details, along with any diagnostics from the superclass
     */
    @Override
    public Map<String, Object> getRequestDiagnostic() {
        Map<String, Object> diagnostic = super.getRequestDiagnostic();

        diagnostic.put("Output Type", getOutputType());
        diagnostic.put("Insert Many JSON", getJsonArray());

        return diagnostic;
    }

    /**
     * Retrieves the output type associated with this operation.
     *
     * @return a {@code String} representing the output type
     */
    public String getOutputType() {
        return outputType;
    }

    /**
     * Sets the output type for the operation.
     *
     * @param outputType the type of output to be used by the operation
     */
    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    /**
     * Retrieves the source of the data to be inserted.
     *
     * @return a String representing the insert source, defining the origin or type of
     *         data being inserted, such as a file, JSON array, or other sources.
     */
    public String getInsertSource() {
        return insertSource;
    }

    /**
     * Sets the source of the insert operation.
     *
     * @param insertSource the source from which data will be inserted. This could represent
     *                     a specific file path, data source name, or identifier that defines
     *                     the origin of the insertion data.
     */
    public void setInsertSource(String insertSource) {
        this.insertSource = insertSource;
    }

    /**
     * Retrieves the source file associated with this operation.
     *
     * @return the source file as a {@link com.appian.connectedsystems.templateframework.sdk.configuration.Document}
     * associated with the operation.
     */
    public com.appian.connectedsystems.templateframework.sdk.configuration.Document getSourceFile() {
        return sourceFile;
    }

    /**
     * Sets the source file for the operation.
     * This file is used as the source of data for the insert operation.
     *
     * @param sourceFile the document representing the source file to be used
     */
    public void setSourceFile(com.appian.connectedsystems.templateframework.sdk.configuration.Document sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Retrieves the value indicating whether the file is an array.
     *
     * @return a {@link Boolean} representing whether the file is an array
     */
    public Boolean getFileIsArray() {
        return fileIsArray;
    }

    /**
     * Sets whether the source file represents an array of documents to be inserted.
     *
     * @param fileIsArray a {@code Boolean} indicating if the source file is an array.
     *                    {@code true} if the source file contains an array of documents,
     *                    {@code false} otherwise.
     */
    public void setFileIsArray(Boolean fileIsArray) {
        this.fileIsArray = fileIsArray;
    }

    /**
     * Retrieves the JSON array associated with this operation.
     *
     * @return a {@code String} representing the JSON array.
     */
    public String getJsonArray() {
        return jsonArray;
    }

    /**
     * Sets the JSON array that is used for the operation.
     *
     * @param jsonArray the JSON array as a {@code String} to be set
     */
    public void setJsonArray(String jsonArray) {
        this.jsonArray = jsonArray;
    }

    /**
     * Retrieves the list of documents for the operation.
     *
     * @return a List of Document objects containing data associated with
     *         this operation.
     */
    public List<Document> getDocuments() {
        return documents;
    }

    /**
     * Sets the list of documents to be inserted in the operation.
     *
     * @param documents a list of {@code Document} objects representing the documents to be inserted
     */
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    /**
     * Retrieves the current setting for skipping date and time conversion.
     *
     * <p>
     * This method returns a Boolean indicating whether the conversion of
     * date and time values should be skipped during the processing of the operation.
     * </p>
     *
     * @return a Boolean value where {@code true} indicates that date and time
     *         conversion is skipped, and {@code false} indicates that conversion
     *         is performed. If no value is set, it may return {@code null}.
     */
    public Boolean getSkipDateTimeConversion() {
        return skipDateTimeConversion;
    }

    /**
     * Sets whether to skip date-time conversion during the operation.
     *
     * @param skipDateTimeConversion a {@link Boolean} indicating whether date-time values
     *                               should be excluded from conversion. If true, date-time
     *                               conversion will be skipped; otherwise, it will be applied.
     */
    public void setSkipDateTimeConversion(Boolean skipDateTimeConversion) {
        this.skipDateTimeConversion = skipDateTimeConversion;
    }
}
