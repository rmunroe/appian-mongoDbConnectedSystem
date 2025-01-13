package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.configuration.*;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.MissingDatabaseException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a set of utility methods for building {@link PropertyDescriptor} objects
 * for an Appian-connected MongoDB integration. Each method in this class configures
 * a particular property or group of properties and adds them to the {@link #propertyDescriptors}
 * list, which will eventually be rendered in the integration UI.
 *
 * <p>
 * Most of these properties are used to capture details such as:
 * <ul>
 *   <li>Which database and collection to interact with</li>
 *   <li>File output and input settings</li>
 *   <li>JSON filter strings, sorting, and projection details for MongoDB queries</li>
 *   <li>Read preference and read concern configurations</li>
 *   <li>Collation settings for language-specific sorting and comparison rules</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class references:
 * <ul>
 *   <li>{@link SimpleIntegrationTemplate} for building complex local-type properties</li>
 *   <li>{@link SimpleConfiguration} for retrieving user-selected or stored integration values</li>
 *   <li>{@link MongoDbUtility} for validating database or collection existence</li>
 *   <li>{@link PropertyDescriptor} for specifying UI fields (like text boxes, folders, documents, etc.)</li>
 * </ul>
 * </p>
 *
 * <p><b>Note:</b> Methods that query MongoDB (e.g., {@link #buildDatabaseProperty()} or
 * {@link #buildCollectionsProperty()}) may silently catch {@link MissingDatabaseException}
 * if the database does not exist, so it does not interrupt property construction.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class PropertyDescriptorsUtil {
    SimpleIntegrationTemplate integrationTemplate;
    SimpleConfiguration integrationConfiguration;
    MongoDbUtility mongoDbUtility;
    List<PropertyDescriptor<?>> propertyDescriptors;

    /**
     * Constructs a new {@code PropertyDescriptorsUtil} with references to the
     * main integration template, configuration, MongoDB utility, and the list
     * of property descriptors to be populated.
     *
     * @param integrationTemplate   the {@link SimpleIntegrationTemplate} for creating complex properties
     * @param integrationConfiguration the {@link SimpleConfiguration} for retrieving or storing property values
     * @param mongoDbUtility        the {@link MongoDbUtility} used for listing databases or collections
     * @param propertyDescriptors   a mutable list of {@link PropertyDescriptor} where new properties will be added
     */
    public PropertyDescriptorsUtil(
            SimpleIntegrationTemplate integrationTemplate,
            SimpleConfiguration integrationConfiguration,
            MongoDbUtility mongoDbUtility,
            List<PropertyDescriptor<?>> propertyDescriptors
    ) {
        this.integrationTemplate = integrationTemplate;
        this.integrationConfiguration = integrationConfiguration;
        this.mongoDbUtility = mongoDbUtility;
        this.propertyDescriptors = propertyDescriptors;
    }

    /**
     * Adds a property descriptor for selecting how results should be returned:
     * as an Appian Dictionary or a JSON array. This populates a dropdown (choice)
     * property for the user.
     */
    public void buildOutputTypeProperty() {
        List<Choice> outputChoices = new ArrayList<>();
        outputChoices.add(Choice.builder().name(MongoDbConnectedSystemConstants.OUTPUT_TYPE_DICTIONARY)
                .value(MongoDbConnectedSystemConstants.OUTPUT_TYPE_DICTIONARY).build());
        outputChoices.add(Choice.builder().name(MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_ARRAY)
                .value(MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_ARRAY).build());

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.OUTPUT_TYPE)
                .label("Output Type")
                .description("How the results should be returned")
                .choices(outputChoices.toArray(new Choice[0]))
                .refresh(RefreshPolicy.ALWAYS)
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );
    }

    /**
     * Adds a boolean property descriptor controlling whether JSON output
     * is merged into a single array or returned as separate objects. This
     * may also show different instructional text if the data is being saved
     * to a file vs. returned in memory.
     *
     * @param writingToFile whether the JSON is being written to a file;
     *                      affects the displayed instructions
     */
    public void buildOutputAsJsonArrayProperty(Boolean writingToFile) {
        String instructions = writingToFile
                ? "Selecting No will return an array of strings"
                : "Selecting No will write one JSON object per line";
        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_FILE_ARRAY)
                .label("Output JSON As a Single Array")
                .instructionText("This will join the results as a JSON array such as: [{...},{...}]. " + instructions)
                .displayMode(BooleanDisplayMode.RADIO_BUTTON)
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );
    }

    /**
     * Configures a series of property descriptors for writing JSON results to an Appian Document.
     * This includes:
     * <ul>
     *   <li>A boolean option to output as a single JSON array</li>
     *   <li>The folder in which to create the output document</li>
     *   <li>The filename to use</li>
     *   <li>The character set (encoding) in which to write the file</li>
     * </ul>
     */
    public void buildFileOutputProperty() {
        buildOutputAsJsonArrayProperty(true);

        propertyDescriptors.add(FolderPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.OUTPUT_FOLDER_ID)
                .label("Save to Folder")
                .description("The Appian Folder where the new Document will be created")
                .isRequired(true)
                .isExpressionable(true)
                .build()
        );
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.OUTPUT_FILE_NAME)
                .label("Filename")
                .description("The name of the output file")
                .placeholder("my_output_file.json")
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );

        Map<String, String> charsets = new HashMap<String, String>() {{
            put("US-ASCII", "Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set");
            put("ISO-8859-1", "ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1");
            put("UTF-8", "Eight-bit UCS Transformation Format");
            put("UTF-16BE", "Sixteen-bit UCS Transformation Format, big-endian byte order");
            put("UTF-16LE", "Sixteen-bit UCS Transformation Format, little-endian byte order");
            put("UTF-16", "Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark");
        }};

        List<Choice> choices = new ArrayList<>();
        charsets.keySet().forEach(charset ->
                choices.add(Choice.builder().name(charset).value(charset).build())
        );

        String charset = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_FILE_CHARSET);
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.OUTPUT_TYPE_JSON_FILE_CHARSET)
                .label("Character Set")
                .description("How the results should be returned")
                .instructionText((StringUtils.isNotBlank(charset)) ? charsets.get(charset) : null)
                .choices(choices.toArray(new Choice[0]))
                .refresh(RefreshPolicy.ALWAYS)
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );
    }

    /**
     * Adds properties for specifying a source JSON file in Appian, and whether that file
     * contains one JSON array or multiple JSON objects (one per line). This is used
     * when inserting JSON documents into MongoDB from a file.
     */
    public void buildFileInputProperty() {
        propertyDescriptors.add(DocumentPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.INSERT_FILE_ID)
                .label("Source JSON File")
                .description("The Appian Document containing the JSON array to be inserted")
                .isRequired(true)
                .isExpressionable(true)
                .build()
        );
        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.INSERT_FILE_IS_ARRAY)
                .label("JSON File Contains a Single Array")
                .instructionText("Check this if the contents of the JSON file are a single JSON array such as: [{...},{...}]. " +
                        "Leave unchecked if there is one JSON object per line in the JSON file.")
                .displayMode(BooleanDisplayMode.RADIO_BUTTON)
                .isExpressionable(true)
                .build()
        );
    }

    /**
     * Adds property descriptors for selecting a MongoDB database and whether to
     * throw an error if that database does not actually exist.
     * <p>
     * Internally, this method queries the database list via {@link MongoDbUtility#listDatabases()}
     * to populate the dropdown choices.
     * </p>
     */
    public void buildDatabaseProperty() {
        // Create list of Database choices for the drop down
        List<Map<String, Object>> databases = mongoDbUtility.listDatabases();
        List<Choice> databaseChoices = new ArrayList<>();
        databases.forEach(db -> databaseChoices.add(
                Choice.builder().name(db.get("name").toString()).value(db.get("name").toString()).build()
        ));

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.DATABASE)
                .label("Database")
                .choices(databaseChoices.toArray(new Choice[0]))
                .description("The MongoDB Database")
                .refresh(RefreshPolicy.ALWAYS)
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );

        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.DATABASE_EXISTS)
                .label("Return error if Database does not exist")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );
    }

    /**
     * Adds property descriptors for selecting a MongoDB collection, based on
     * the previously selected database. Also includes a boolean property
     * determining whether an error should be thrown if the collection is missing.
     * <p>
     * Note that if the chosen database does not actually exist,
     * {@link MissingDatabaseException} may be thrown internally (and caught)
     * when listing collections.
     * </p>
     */
    public void buildCollectionsProperty() {
        Object database = integrationConfiguration.getValue(MongoDbConnectedSystemConstants.DATABASE);
        if (database != null) {
            List<Choice> collectionChoices = new ArrayList<>();

            TextPropertyDescriptor.TextPropertyDescriptorBuilder propertyDescriptorBuilder = TextPropertyDescriptor.builder()
                    .key(MongoDbConnectedSystemConstants.COLLECTION)
                    .label("Collection")
                    .description("The MongoDB Collection")
                    .refresh(RefreshPolicy.ALWAYS)
                    .isExpressionable(true)
                    .isRequired(true);

            if (database instanceof String) {
                try {
                    List<Map<String, Object>> collections = mongoDbUtility.listCollections((String) database, true, true);
                    collections.forEach(col -> collectionChoices.add(
                            Choice.builder().name(col.get("name").toString()).value(col.get("name").toString()).build()
                    ));
                    propertyDescriptorBuilder.choices(collectionChoices.toArray(new Choice[0]));
                } catch (MissingDatabaseException ignored) {
                    // If the DB is missing, we simply don't populate the collection choices
                }
            }

            propertyDescriptors.add(propertyDescriptorBuilder.build());

            propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                    .key(MongoDbConnectedSystemConstants.COLLECTION_EXISTS)
                    .label("Return error if Collection does not exist")
                    .isExpressionable(true)
                    .isRequired(false)
                    .build()
            );
        }
    }

    /**
     * Adds a property descriptor for selecting a MongoDB {@code ReadPreference},
     * which describes how clients route read operations to the members of a replica set.
     */
    public void buildReadPreferenceProperty() {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.READ_PREFERENCE)
                .label("Read Preference")
                .choices(
                        Choice.builder().name("Primary").value(MongoDbConnectedSystemConstants.READ_PREFERENCE_PRIMARY).build(),
                        Choice.builder().name("Primary Preferred").value(MongoDbConnectedSystemConstants.READ_PREFERENCE_PRIMARY_PREFERRED).build(),
                        Choice.builder().name("Secondary").value(MongoDbConnectedSystemConstants.READ_PREFERENCE_SECONDARY).build(),
                        Choice.builder().name("Secondary Preferred").value(MongoDbConnectedSystemConstants.READ_PREFERENCE_SECONDARY_PREFERRED).build(),
                        Choice.builder().name("Nearest").value(MongoDbConnectedSystemConstants.READ_PREFERENCE_NEAREST).build()
                )
                .description("Read preference describes how MongoDB clients route read operations to the members of a replica set")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );
    }

    /**
     * Adds a property descriptor for selecting a MongoDB {@code ReadConcern},
     * which allows you to control the consistency and isolation properties
     * of the data read from replica sets and shards.
     */
    public void buildReadConcernProperty() {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.READ_CONCERN)
                .label("Read Concern")
                .choices(
                        Choice.builder().name("Local").value(MongoDbConnectedSystemConstants.READ_CONCERN_LOCAL).build(),
                        Choice.builder().name("Available").value(MongoDbConnectedSystemConstants.READ_CONCERN_AVAILABLE).build(),
                        Choice.builder().name("Majority").value(MongoDbConnectedSystemConstants.READ_CONCERN_MAJORITY).build(),
                        Choice.builder().name("Linearizable").value(MongoDbConnectedSystemConstants.READ_CONCERN_LINEARIZABLE).build(),
                        Choice.builder().name("Snapshot").value(MongoDbConnectedSystemConstants.READ_CONCERN_SNAPSHOT).build()
                )
                .description("Allows you to control the consistency and isolation properties of the data read from replica sets and replica set shards")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );
    }

    /**
     * Creates a local-type descriptor for MongoDB Collation options and adds it
     * as a property descriptor. Collation allows users to specify language-specific
     * rules for string comparison, such as rules for lettercase and accent marks.
     * <p>
     * This includes sub-properties for locale, case level, strength, numeric ordering, etc.
     * </p>
     */
    public void buildCollationsProperty() {
        propertyDescriptors.add(this.integrationTemplate.localTypeProperty(
                        LocalTypeDescriptor.builder().name(MongoDbConnectedSystemConstants.COLLATION).properties(
                                TextPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_LOCALE).label("Locale").description("The ICU locale.").build(),
                                BooleanPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_CASE_LEVEL).label("Case Level").displayMode(BooleanDisplayMode.RADIO_BUTTON).build(),
                                TextPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_CASE_FIRST).label("Case First").build(),
                                IntegerPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_STRENGTH).label("Strength").build(),
                                BooleanPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_NUMERIC_ORDERING).label("Numeric Ordering").displayMode(BooleanDisplayMode.RADIO_BUTTON).build(),
                                TextPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_ALTERNATE).label("Alternate").build(),
                                TextPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_MAX_VARIABLE).label("Max Variable").build(),
                                BooleanPropertyDescriptor.builder().key(MongoDbConnectedSystemConstants.COLLATION_BACKWARDS).label("Backwards").displayMode(BooleanDisplayMode.RADIO_BUTTON).build()
                        ).build())
                .isExpressionable(true)
                .label("Collation")
                .description("Collation allows users to specify language-specific rules for string comparison, such as rules for lettercase and accent marks.")
                .isExpressionable(true)
                .build()
        );
    }

    /**
     * Adds a property descriptor for specifying a JSON filter document. If
     * {@code required} is false, the user is warned that leaving this blank
     * will match ALL documents in the collection.
     *
     * @param required whether the filter JSON field is required
     */
    public void buildFilterJsonProperty(Boolean required) {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.FILTER_JSON)
                .label("Filter JSON")
                .description("A JSON string representing a MongoDB query filter Document")
                .instructionText(required ? null : "WARNING: leaving Filter JSON blank will match ALL Documents in the Collection")
                .isExpressionable(true)
                .displayHint(DisplayHint.EXPRESSION)
                .isRequired(required)
                .build()
        );
    }

    /**
     * Adds property descriptors for configuring a MongoDB find operation:
     * <ul>
     *   <li>{@link #buildFilterJsonProperty(Boolean)} for the query filter</li>
     *   <li>Sort JSON for specifying the result order</li>
     *   <li>Projection JSON for limiting returned fields</li>
     *   <li>Limit, Skip, Collation, and maximum processing time</li>
     *   <li>Read preference/concern properties</li>
     *   <li>A boolean for including the internal record ID</li>
     * </ul>
     * These fields collectively define a typical MongoDB {@code find()} query in Appian.
     */
    public void buildCollectionFindProperties() {
        buildFilterJsonProperty(false);

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.SORT_JSON)
                .label("Sort JSON")
                .description("A JSON string representing the sort order for a Collection.Find() query. Sort specifies the order in which the query returns matching documents.")
                .isExpressionable(true)
                .displayHint(DisplayHint.EXPRESSION)
                .isRequired(false)
                .build()
        );

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.PROJECTION_JSON)
                .label("Projection JSON")
                .description("A JSON string representing a Projection for a Collection.Find() query. Projections limit the amount of data that MongoDB returns.")
                .isExpressionable(true)
                .displayHint(DisplayHint.EXPRESSION)
                .isRequired(false)
                .build()
        );

        propertyDescriptors.add(IntegerPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.LIMIT)
                .label("Limit")
                .description("The number of results to return. Useful for paging.")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );

        propertyDescriptors.add(IntegerPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.SKIP)
                .label("Skip")
                .description("The number of results to skip. Useful for paging.")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );

        buildCollationsProperty();

        propertyDescriptors.add(IntegerPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.MAX_TIME)
                .label("Max Processing Time")
                .description("Specifies a cumulative time limit in milliseconds for processing operations on a Find operation.")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );

        buildReadPreferenceProperty();
        buildReadConcernProperty();

        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.RECORD_ID)
                .label("Include Record Id")
                .description("Record Id is the internal key which uniquely identifies a Document in a Collection. This is different from a Document's Object Id.")
                .displayMode(BooleanDisplayMode.RADIO_BUTTON)
                .isExpressionable(true)
                .build()
        );
    }

    /**
     * Adds property descriptors to configure a MongoDB aggregation pipeline.
     * This includes:
     * <ul>
     *   <li>A JSON array of pipeline stages</li>
     *   <li>An example pipeline descriptor (read-only) for reference</li>
     * </ul>
     */
    public void buildCollectionAggregateProperties() {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.AGGREGATE_PIPELINE_JSON)
                .label("Aggregate Pipeline Stages JSON")
                .description("A JSON string representing an Array of BSON Documents that define an Aggregate Pipeline. See example below.")
                .isExpressionable(true)
                .displayHint(DisplayHint.EXPRESSION)
                .isRequired(true)
                .build()
        );

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key("ExamplePipeline")
                .label("Example 3-Stage Pipeline")
                .isReadOnly(true)
                .instructionText("This aggregate would return the 10 most common last names")
                .build()
        );
    }

    /**
     * Adds a boolean property descriptor to control whether the automatic
     * date/time conversion (from ISO date strings to {@code ISODate}) should
     * be skipped when inserting new documents.
     * <p>
     * By default, any recognized ISO date/time strings are converted into actual
     * MongoDB {@code Date} objects. Enabling this option preserves the strings
     * instead.
     * </p>
     */
    public void buildInsertOptionsProperties() {
        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(MongoDbConnectedSystemConstants.INSERT_SKIP_DATETIME_CONVERSION)
                .label("Skip Automatic Date Time Conversion")
                .instructionText("By default any String that matches the ISO Date format (such as Appian does when serializing Dates and Date Times to JSON) will be automatically converted to the MongoDB ISODate() datatype. Selecting Yes will skip this conversion and simply store it as a String.")
                .displayMode(BooleanDisplayMode.RADIO_BUTTON)
                .isExpressionable(true)
                .build()
        );
    }
}