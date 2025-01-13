package com.appiancorp.solutionsconsulting.plugin.mongodb;

/**
 * Provides a centralized collection of constants used throughout the MongoDB
 * Connected System plugin for Appian.
 *
 * <p>UPPER_CASE constants serve as keys for property descriptors.
 * Non-UPPER_CASE values typically appear as displayed option choices in
 * the MongoDB Integrations.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class MongoDbConnectedSystemConstants {

    /**
     * Key for specifying the output type property descriptor.
     */
    public static final String OUTPUT_TYPE = "OUTPUT_TYPE";

    /**
     * Display label for selecting "Dictionary" as an output type.
     */
    public static final String OUTPUT_TYPE_DICTIONARY = "Dictionary";

    /**
     * Display label for selecting "List of JSON Strings" as an output type.
     */
    public static final String OUTPUT_TYPE_JSON_ARRAY = "List of JSON Strings";

    /**
     * Key for specifying that the output should be a JSON file array.
     */
    public static final String OUTPUT_TYPE_JSON_FILE_ARRAY = "OUTPUT_TYPE_JSON_FILE_ARRAY";

    /**
     * Key for specifying the character set to be used in the JSON file output.
     */
    public static final String OUTPUT_TYPE_JSON_FILE_CHARSET = "OUTPUT_TYPE_JSON_FILE_CHARSET";

    /**
     * Key for specifying the folder ID where output files are stored.
     */
    public static final String OUTPUT_FOLDER_ID = "OUTPUT_FOLDER_ID";

    /**
     * Key for specifying the name of the output file.
     */
    public static final String OUTPUT_FILE_NAME = "OUTPUT_FILE_NAME";

    /**
     * Key for specifying the MongoDB database name.
     */
    public static final String DATABASE = "DATABASE";

    /**
     * Key for indicating that the database must exist (validated in the UI).
     */
    public static final String DATABASE_EXISTS = "DATABASE_EXISTS";

    /**
     * Key for specifying the MongoDB collection name.
     */
    public static final String COLLECTION = "COLLECTION";

    /**
     * Key for indicating that the collection must exist (validated in the UI).
     */
    public static final String COLLECTION_EXISTS = "COLLECTION_EXISTS";

    /**
     * Key for specifying the read preference in MongoDB operations.
     */
    public static final String READ_PREFERENCE = "READ_PREFERENCE";

    /**
     * Key for specifying the "primary" read preference.
     */
    public static final String READ_PREFERENCE_PRIMARY = "READ_PREFERENCE_PRIMARY";

    /**
     * Key for specifying the "primaryPreferred" read preference.
     */
    public static final String READ_PREFERENCE_PRIMARY_PREFERRED = "READ_PREFERENCE_PRIMARY_PREFERRED";

    /**
     * Key for specifying the "secondary" read preference.
     */
    public static final String READ_PREFERENCE_SECONDARY = "READ_PREFERENCE_SECONDARY";

    /**
     * Key for specifying the "secondaryPreferred" read preference.
     */
    public static final String READ_PREFERENCE_SECONDARY_PREFERRED = "READ_PREFERENCE_SECONDARY_PREFERRED";

    /**
     * Key for specifying the "nearest" read preference.
     */
    public static final String READ_PREFERENCE_NEAREST = "READ_PREFERENCE_NEAREST";

    /**
     * Key for specifying the read concern in MongoDB operations.
     */
    public static final String READ_CONCERN = "READ_CONCERN";

    /**
     * Key for specifying the "local" read concern.
     */
    public static final String READ_CONCERN_LOCAL = "READ_CONCERN_LOCAL";

    /**
     * Key for specifying the "available" read concern.
     */
    public static final String READ_CONCERN_AVAILABLE = "READ_CONCERN_AVAILABLE";

    /**
     * Key for specifying the "majority" read concern.
     */
    public static final String READ_CONCERN_MAJORITY = "READ_CONCERN_MAJORITY";

    /**
     * Key for specifying the "linearizable" read concern.
     */
    public static final String READ_CONCERN_LINEARIZABLE = "READ_CONCERN_LINEARIZABLE";

    /**
     * Key for specifying the "snapshot" read concern.
     */
    public static final String READ_CONCERN_SNAPSHOT = "READ_CONCERN_SNAPSHOT";

    /**
     * Key for specifying a collation object in MongoDB operations.
     */
    public static final String COLLATION = "COLLATION";

    /**
     * Key for specifying the locale in the collation settings.
     */
    public static final String COLLATION_LOCALE = "COLLATION_LOCALE";

    /**
     * Key for specifying case level in the collation settings.
     */
    public static final String COLLATION_CASE_LEVEL = "COLLATION_CASE_LEVEL";

    /**
     * Key for specifying case order in the collation settings.
     */
    public static final String COLLATION_CASE_FIRST = "COLLATION_CASE_FIRST";

    /**
     * Key for specifying the strength in the collation settings.
     */
    public static final String COLLATION_STRENGTH = "COLLATION_STRENGTH";

    /**
     * Key for specifying numeric ordering in the collation settings.
     */
    public static final String COLLATION_NUMERIC_ORDERING = "COLLATION_NUMERIC_ORDERING";

    /**
     * Key for specifying the alternate collation settings.
     */
    public static final String COLLATION_ALTERNATE = "COLLATION_ALTERNATE";

    /**
     * Key for specifying the maximum variable in the collation settings.
     */
    public static final String COLLATION_MAX_VARIABLE = "COLLATION_MAX_VARIABLE";

    /**
     * Key for specifying the backwards setting in the collation settings.
     */
    public static final String COLLATION_BACKWARDS = "COLLATION_BACKWARDS";

    /**
     * Key for specifying a filter in JSON format for MongoDB operations.
     */
    public static final String FILTER_JSON = "FILTER_JSON";

    /**
     * Key for specifying a sort in JSON format for MongoDB operations.
     */
    public static final String SORT_JSON = "SORT_JSON";

    /**
     * Key for specifying a projection in JSON format for MongoDB operations.
     */
    public static final String PROJECTION_JSON = "PROJECTION_JSON";

    /**
     * Key for specifying a limit on the number of returned documents.
     */
    public static final String LIMIT = "LIMIT";

    /**
     * Key for specifying a skip value (how many documents to skip).
     */
    public static final String SKIP = "SKIP";

    /**
     * Key for specifying a record ID (e.g., for single-document operations).
     */
    public static final String RECORD_ID = "RECORD_ID";

    /**
     * Key for specifying a maximum time in milliseconds for operations.
     */
    public static final String MAX_TIME = "MAX_TIME";

    /**
     * Key for specifying an aggregation pipeline in JSON format.
     */
    public static final String AGGREGATE_PIPELINE_JSON = "AGGREGATE_PIPELINE_JSON";

    /**
     * Key for specifying the source type (JSON string or file) during inserts.
     */
    public static final String INSERT_SOURCE = "INSERT_SOURCE";

    /**
     * Display label for selecting "JSON String" as the insert source.
     */
    public static final String INSERT_SOURCE_JSON = "JSON String";

    /**
     * Display label for selecting "JSON from Appian Document" as the insert source.
     */
    public static final String INSERT_SOURCE_DOCUMENT = "JSON from Appian Document";

    /**
     * Key for specifying the file ID for insert operations.
     */
    public static final String INSERT_FILE_ID = "INSERT_FILE_ID";

    /**
     * Key for indicating if an inserted file contains an array of documents.
     */
    public static final String INSERT_FILE_IS_ARRAY = "INSERT_FILE_IS_ARRAY";

    /**
     * Key for specifying JSON data for an insert-many operation.
     */
    public static final String INSERT_MANY_JSON = "INSERT_MANY_JSON";

    /**
     * Key for specifying JSON data for an insert-one operation.
     */
    public static final String INSERT_ONE_JSON = "INSERT_ONE_JSON";

    /**
     * Key for indicating whether to skip date/time conversion during insert.
     */
    public static final String INSERT_SKIP_DATETIME_CONVERSION = "INSERT_SKIP_DATETIME_CONVERSION";

    /**
     * Key for specifying JSON data for an update operation.
     */
    public static final String UPDATE_JSON = "UPDATE_JSON";

    /**
     * Key for specifying JSON data for a replace-one operation.
     */
    public static final String REPLACE_ONE_JSON = "REPLACE_ONE_JSON";

    /**
     * Key for specifying JSON data to create an index in a collection.
     */
    public static final String INDEX_JSON = "INDEX_JSON";

    /**
     * Key for specifying JSON data in a bulk write operation.
     */
    public static final String BULK_WRITE_JSON = "BULK_WRITE_JSON";

    /**
     * Key for specifying whether the bulk write operation is ordered.
     */
    public static final String BULK_WRITE_IS_ORDERED = "BULK_WRITE_IS_ORDERED";

    /**
     * Key for specifying the write concern in a bulk write operation.
     */
    public static final String BULK_WRITE_CONCERN = "BULK_WRITE_CONCERN";
}