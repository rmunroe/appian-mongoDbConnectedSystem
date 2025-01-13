package com.appiancorp.solutionsconsulting.plugin.mongodb;

import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.Base64;
import java.util.UUID;

/**
 * A utility class for preparing and sanitizing MongoDB {@link Document} instances
 * before returning them to Appian or inserting them into a database. This includes:
 * <ul>
 *   <li>Recursively converting nested {@link Document} objects.</li>
 *   <li>Handling {@link ObjectId}, {@link UUID}, and {@link Binary} fields in a
 *       way that’s more Appian-friendly.</li>
 *   <li>Optionally converting string fields that match ISO date/time formats
 *       into MongoDB date objects for insertion.</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Example: Convert nested ObjectIds or UUIDs to strings for output
 * Document doc = ... // retrieved from MongoDB
 * Document cleanedDoc = MongoDocumentUtil.prepDocumentForOutput(doc, true, true);
 * // cleanedDoc now has "oid" fields and UUIDs replaced with their string versions
 *
 * // Example: Convert date-like strings to MongoDB date objects before insertion
 * Document toInsert = new Document("eventDate", "2024-12-31T23:59:59Z");
 * Document preppedInsert = MongoDocumentUtil.prepDocumentForInsert(toInsert, false);
 * // preppedInsert now has a MongoDB date object for "eventDate"
 * }</pre>
 *
 * <p>Both methods in this class are designed to operate recursively on any
 * nested {@link Document} objects they encounter.</p>
 *
 * <p><b>Note:</b> This class makes basic assumptions about date/time formats
 * (ISO date/time strings). If your fields might have other date formats, you
 * may need to customize the regex checks or the conversion logic.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class MongoDocumentUtil {

    /**
     * A regular expression matching the date portion of an ISO-8601 date,
     * including optional negative years (to handle extended ISO-8601).
     */
    private static final String isoDatePattern = "(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])";

    /**
     * A regular expression matching the time portion of an ISO-8601 date/time,
     * including optional fractional seconds and timezone offset.
     */
    private static final String isoTimePattern = "T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?";

    /**
     * A regular expression combining {@link #isoDatePattern} and {@link #isoTimePattern}
     * to match full ISO-8601 date/time strings.
     */
    private static final String isoDateTimePattern = isoDatePattern + isoTimePattern;

    /**
     * A regular expression used for detecting date-only strings that have a trailing 'Z',
     * e.g., "2024-12-31Z".
     */
    private static final String appianDatePattern = isoDatePattern + "Z$";

    /**
     * Cleans and normalizes a {@link Document} by:
     * <ul>
     *   <li>Recursively calling itself on nested {@code Document} values.</li>
     *   <li>Replacing {@link ObjectId} instances with a sub-document containing an "oid" field
     *       if {@code objectIdAsString} is true.</li>
     *   <li>Encoding {@link Binary} data into a base64 string, along with a "type" field
     *       that denotes the binary subtype.</li>
     *   <li>Replacing {@link UUID} instances with their string representation
     *       if {@code uuidAsString} is true.</li>
     * </ul>
     *
     * <p>This method is typically used when preparing MongoDB documents for
     * return to an Appian integration, ensuring that certain BSON types are
     * represented in a more JSON-friendly format.</p>
     *
     * @param document          the MongoDB {@link Document} to clean
     * @param objectIdAsString  if true, replaces {@link ObjectId} with a sub-document
     *                          that contains a string-based "oid"
     * @param uuidAsString      if true, replaces {@link UUID} with a string representation
     * @return the cleaned {@link Document}, potentially with updated or nested fields
     */
    public static Document prepDocumentForOutput(Document document, Boolean objectIdAsString, Boolean uuidAsString) {
        for (String key : document.keySet()) {
            Object val = document.get(key);

            if (val instanceof Document) {
                // Recursively handle nested documents
                document.put(key, prepDocumentForOutput((Document) val, objectIdAsString, uuidAsString));

            } else if (Boolean.TRUE.equals(objectIdAsString) && val instanceof ObjectId) {
                // Replace ObjectId with a sub-document containing the string form
                Document oid = new Document();
                oid.put("oid", val.toString());
                document.put(key, oid);

            } else if (val instanceof Binary) {
                // Convert Binary data to base64
                Document binary = new Document();
                binary.put("binary", Base64.getEncoder().encodeToString(((Binary) val).getData()));
                binary.put("type", String.valueOf(((Binary) val).getType()));
                document.put(key, binary);

            } else if (Boolean.TRUE.equals(uuidAsString) && val instanceof UUID) {
                // Replace UUID with its string representation
                document.put(key, val.toString());
            }
        }
        return document;
    }

    /**
     * Prepares a {@link Document} for insertion into MongoDB by:
     * <ul>
     *   <li>Recursively calling itself on nested {@code Document} values.</li>
     *   <li>Detecting ISO date/time strings (e.g., {@code 2024-12-31T23:59:59Z})
     *       and converting them into {@code ISODate} objects (unless
     *       {@code skipDateTimeConversion} is true).</li>
     *   <li>Handling date-only strings that match {@link #appianDatePattern} by inserting
     *       a zero-time component (e.g., {@code T00:00:00.000Z}).</li>
     * </ul>
     *
     * @param document              the MongoDB {@link Document} to prepare for insertion
     * @param skipDateTimeConversion if true, does not attempt any date/time conversion
     * @return the {@link Document} after potential date/time conversions
     */
    public static Document prepDocumentForInsert(Document document, Boolean skipDateTimeConversion) {
        for (String key : document.keySet()) {
            Object val = document.get(key);

            if (val instanceof Document) {
                // Recursively handle nested documents
                document.put(key, prepDocumentForInsert((Document) val, skipDateTimeConversion));

            } else if (!Boolean.TRUE.equals(skipDateTimeConversion) && val instanceof String) {
                String strVal = (String) val;

                // If it matches a full ISO date/time, parse it into an ISODate
                if (strVal.matches(isoDateTimePattern)) {
                    Document parsed = Document.parse("{ \"" + key + "\": ISODate(\"" + strVal + "\") }");
                    document.put(key, parsed.get(key));

                    // If it matches a date-only string, add a zero-time component
                } else if (strVal.matches(appianDatePattern)) {
                    String adjusted = strVal.replaceFirst("Z$", "") + "T00:00:00.000Z";
                    Document parsed = Document.parse("{ \"" + key + "\": ISODate(\"" + adjusted + "\") }");
                    document.put(key, parsed.get(key));
                }
            }
        }
        return document;
    }
}