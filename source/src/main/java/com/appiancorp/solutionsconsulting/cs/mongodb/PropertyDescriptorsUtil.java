package com.appiancorp.solutionsconsulting.cs.mongodb;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.configuration.*;
import com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions.MissingDatabaseException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appiancorp.solutionsconsulting.cs.mongodb.MongoDbConnectedSystemConstants.*;

public class PropertyDescriptorsUtil {
    SimpleIntegrationTemplate integrationTemplate;
    SimpleConfiguration integrationConfiguration;
    MongoDbUtility mongoDbUtility;
    List<PropertyDescriptor<?>> propertyDescriptors;

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

    public void buildOutputTypeProperty() {
        List<Choice> outputChoices = new ArrayList<>();
        outputChoices.add(Choice.builder().name(OUTPUT_TYPE_DICTIONARY).value(OUTPUT_TYPE_DICTIONARY).build());
        outputChoices.add(Choice.builder().name(OUTPUT_TYPE_JSON_ARRAY).value(OUTPUT_TYPE_JSON_ARRAY).build());

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(OUTPUT_TYPE)
                .label("Output Type")
                .description("How the results should be returned")
                .choices(outputChoices.toArray(new Choice[0]))
                .refresh(RefreshPolicy.ALWAYS)
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );
    }


    public void buildFileOutputProperty() {
        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(OUTPUT_TYPE_JSON_FILE_ARRAY)
                .label("Output JSON as a single array")
                .instructionText("This will join the results as a JSON array such as: [{...},{...}]. Leaving unchecked will write one JSON object per line.")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );
        propertyDescriptors.add(FolderPropertyDescriptor.builder()
                .key(OUTPUT_FOLDER_ID)
                .label("Save to Folder")
                .description("The Appian Folder where the new Document will be created")
                .isRequired(true)
                .isExpressionable(true)
                .build()
        );
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(OUTPUT_FILE_NAME)
                .label("Filename")
                .description("The name of the output file")
                .placeholder("my_output_file.json")
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );

        HashMap<String, String> charsets = new HashMap<>();
        charsets.put("US-ASCII", "Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set");
        charsets.put("ISO-8859-1", "ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1");
        charsets.put("UTF-8", "Eight-bit UCS Transformation Format");
        charsets.put("UTF-16BE", "Sixteen-bit UCS Transformation Format, big-endian byte order");
        charsets.put("UTF-16LE", "Sixteen-bit UCS Transformation Format, little-endian byte order");
        charsets.put("UTF-16", "Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark");
        List<Choice> choices = new ArrayList<>();
        charsets.keySet().forEach(charset -> choices.add(Choice.builder().name(charset).value(charset).build()));

        String charset = integrationConfiguration.getValue(OUTPUT_TYPE_JSON_FILE_CHARSET);
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(OUTPUT_TYPE_JSON_FILE_CHARSET)
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


    public void buildDatabaseProperty() {
        // Create list of Database choices for the drop down
        List<Map<String, Object>> databases = mongoDbUtility.listDatabases();
        List<Choice> databaseChoices = new ArrayList<>();
        databases.forEach(db -> databaseChoices.add(
                Choice.builder().name(db.get("name").toString()).value(db.get("name").toString()).build()
        ));

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(DATABASE)
                .label("Database")
                .choices(databaseChoices.toArray(new Choice[0]))
                .description("The MongoDB Database to list Collections from")
                .refresh(RefreshPolicy.ALWAYS)
                .isExpressionable(true)
                .isRequired(true)
                .build()
        );

        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(DATABASE_EXISTS)
                .label("Return error if Database does not exist")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );
    }

    public void buildCollectionsProperty() {
        Object database = integrationConfiguration.getValue(DATABASE);
        if (database != null) {
            List<Choice> collectionChoices = new ArrayList<>();

            TextPropertyDescriptor.TextPropertyDescriptorBuilder propertyDescriptorBuilder = TextPropertyDescriptor.builder()
                    .key(COLLECTION)
                    .label("Collection")
                    .description("The MongoDB Collection to Find from")
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
                }
            }

            propertyDescriptors.add(propertyDescriptorBuilder.build());

            propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                    .key(COLLECTION_EXISTS)
                    .label("Return error if Collection does not exist")
                    .isExpressionable(true)
                    .isRequired(false)
                    .build()
            );
        }
    }


    public void buildReadPreferenceProperty() {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(READ_PREFERENCE)
                .label("Read Preference")
                .choices(
                        Choice.builder().name("Primary").value(READ_PREFERENCE_PRIMARY).build(),
                        Choice.builder().name("Primary Preferred").value(READ_PREFERENCE_PRIMARYPREFERRED).build(),
                        Choice.builder().name("Secondary").value(READ_PREFERENCE_SECONDARY).build(),
                        Choice.builder().name("Secondary Preferred").value(READ_PREFERENCE_SECONDARYPREFERRED).build(),
                        Choice.builder().name("Nearest").value(READ_PREFERENCE_NEAREST).build()
                )
                .description("Read preference describes how MongoDB clients route read operations to the members of a replica set")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );
    }

    public void buildReadConcernProperty() {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(READ_CONCERN)
                .label("Read Concern")
                .choices(
                        Choice.builder().name("Local").value(READ_CONCERN_LOCAL).build(),
                        Choice.builder().name("Available").value(READ_CONCERN_AVAILABLE).build(),
                        Choice.builder().name("Majority").value(READ_CONCERN_MAJORITY).build(),
                        Choice.builder().name("Linearizable").value(READ_CONCERN_LINEARIZABLE).build(),
                        Choice.builder().name("Snapshot").value(READ_CONCERN_SNAPSHOT).build()
                )
                .description("Allows you to control the consistency and isolation properties of the data read from replica sets and replica set shards")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );
    }


    public void buildCollationsProperty() {
        propertyDescriptors.add(this.integrationTemplate.localTypeProperty(
                LocalTypeDescriptor.builder().name(COLLATION).properties(
                        TextPropertyDescriptor.builder().key(COLLATION_LOCALE).label("Locale").description("The ICU locale.").build(),
                        BooleanPropertyDescriptor.builder().key(COLLATION_CASE_LEVEL).label("Case Level").displayMode(BooleanDisplayMode.RADIO_BUTTON).build(),
                        TextPropertyDescriptor.builder().key(COLLATION_CASE_FIRST).label("Case First").build(),
                        IntegerPropertyDescriptor.builder().key(COLLATION_STRENGTH).label("Strength").build(),
                        BooleanPropertyDescriptor.builder().key(COLLATION_NUMERIC_ORDERING).label("Numeric Ordering").displayMode(BooleanDisplayMode.RADIO_BUTTON).build(),
                        TextPropertyDescriptor.builder().key(COLLATION_ALTERNATE).label("Alternate").build(),
                        TextPropertyDescriptor.builder().key(COLLATION_MAX_VARIABLE).label("Max Variable").build(),
                        BooleanPropertyDescriptor.builder().key(COLLATION_BACKWARDS).label("Backwards").displayMode(BooleanDisplayMode.RADIO_BUTTON).build()
                ).build())
                .isExpressionable(true)
                .label("Collation")
                .description("Collation allows users to specify language-specific rules for string comparison, such as rules for lettercase and accent marks.")
                .isExpressionable(true)
                .build()
        );
    }


    public void buildCollectionFindProperties() {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(FILTER_JSON)
                .label("Query Filter JSON")
                .description("A JSON string representing a Collection.Find() filter Document")
                .instructionText("WARNING: leaving Query Filter JSON blank will return ALL Documents in the Collection")
                .isExpressionable(true)
                .displayHint(DisplayHint.EXPRESSION)
                .isRequired(false)
                .build()
        );

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(SORT_JSON)
                .label("Sort JSON")
                .description("A JSON string representing the sort order for a Collection.Find() query. Sort specifies the order in which the query returns matching documents.")
                .isExpressionable(true)
                .displayHint(DisplayHint.EXPRESSION)
                .isRequired(false)
                .build()
        );

        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(PROJECTION_JSON)
                .label("Projection JSON")
                .description("A JSON string representing a Projection for a Collection.Find() query. Projections limit the amount of data that MongoDB returns.")
                .isExpressionable(true)
                .displayHint(DisplayHint.EXPRESSION)
                .isRequired(false)
                .build()
        );

        propertyDescriptors.add(IntegerPropertyDescriptor.builder()
                .key(LIMIT)
                .label("Limit")
                .description("The number of results to return. Useful for paging.")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );

        propertyDescriptors.add(IntegerPropertyDescriptor.builder()
                .key(SKIP)
                .label("Skip")
                .description("The number of results to skip. Useful for paging.")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );

        buildCollationsProperty();

        propertyDescriptors.add(IntegerPropertyDescriptor.builder()
                .key(MAX_TIME)
                .label("Max Processing Time")
                .description("Specifies a cumulative time limit in milliseconds for processing operations on a Find operation.")
                .isExpressionable(true)
                .isRequired(false)
                .build()
        );

        buildReadPreferenceProperty();
        buildReadConcernProperty();

        propertyDescriptors.add(BooleanPropertyDescriptor.builder()
                .key(RECORD_ID)
                .label("Include Record Id")
                .description("Record Id is the internal key which uniquely identifies a Document in a Collection. This is different from a Document's Object Id.")
                .displayMode(BooleanDisplayMode.RADIO_BUTTON)
                .isExpressionable(true)
                .build()
        );
    }

    public void buildCollectionAggregateProperties() {
        propertyDescriptors.add(TextPropertyDescriptor.builder()
                .key(AGGREGATE_PIPELINE_JSON)
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
}
