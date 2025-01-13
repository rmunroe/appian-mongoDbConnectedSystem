package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.core.data.DateWithTimezone;
import com.appiancorp.core.data.TimestampWithTimezone;
import com.appiancorp.ps.plugins.typetransformer.AppianTypeFactory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.AppianTypeHelper;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Binary;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Point;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId;
import com.appiancorp.suiteapi.type.Datatype;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provides a set of utility methods for converting Appian {@link TypedValue} instances into MongoDB-compatible
 * JSON and {@link Document} objects. This class includes helper functions for building
 * MongoDB query expressions, validating JSON, and handling special Appian data types such as
 * {@link com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Binary},
 * {@link com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId}, and
 * {@link com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Point}.
 *
 * <p>Many of these methods leverage the Appian {@link TypeService} to properly interpret
 * and transform CDT or Dictionary structures into JSON-like formats suitable for MongoDB.
 * Additionally, it handles date/time and boolean nuances specific to Appian typed values.</p>
 *
 * <p>Usage typically involves calling static methods like:
 * <ul>
 *   <li>{@link #typedValueToDocument(TypeService, TypedValue)} to convert a typed value into a {@link Document}</li>
 *   <li>{@link #buildBasicOperator(TypeService, String, TypedValue)} to build a MongoDB operator expression</li>
 *   <li>{@link #isValidJson(String)} to verify JSON validity</li>
 * </ul>
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class MongoDbJsonHelper {
    /**
     * Converts an Appian {@link TypedValue} (representing a Dictionary/CDT) into a MongoDB {@link Document}.
     *
     * @param typeService  the Appian TypeService used to interpret typed values
     * @param typedValue   the typed value containing a Dictionary or CDT
     * @return a {@link Document} that can be inserted into MongoDB
     * @throws JAXBException   if an error occurs during XML-to-Java binding
     * @throws ParseException  if an error occurs while parsing date/time values
     */
    public static Document typedValueToDocument(TypeService typeService, TypedValue typedValue) throws JAXBException, ParseException {
        AppianTypeFactory typeFactory = AppianTypeFactory.newInstance(typeService);

        Document doc = new Document();

        Datatype binaryType = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MongoDB", "Binary"));
        Datatype objectIdType = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MongoDB", "ObjectId"));

        @SuppressWarnings("unchecked")
        Map<TypedValue, TypedValue> dictionary = (HashMap<TypedValue, TypedValue>) typeService.cast(AppianTypeLong.DICTIONARY, typedValue).getValue();
        for (TypedValue key : dictionary.keySet()) {
            String keyName = key.getValue().toString();

            if (dictionary.get(key).getInstanceType().equals(binaryType.getId())) {
                // Is Binary
                Binary ourBin = typeFactory.toJavaObject(dictionary.get(key), Binary.class);
                org.bson.types.Binary value = new org.bson.types.Binary(
                        (byte) ((byte) 48 - ourBin.getType().getBytes()[0]),
                        Base64.getDecoder().decode(ourBin.getBinary().getBytes())
                );
                doc.put(keyName, value);

            } else if (dictionary.get(key).getInstanceType().equals(objectIdType.getId())) {
                // Is ObjectID
                ObjectId ourOid = typeFactory.toJavaObject(dictionary.get(key), ObjectId.class);
                org.bson.types.ObjectId value = new org.bson.types.ObjectId(ourOid.getOid());
                doc.put(keyName, value);

            } else {
                Object value = dictionary.get(key).getValue();

                if (value instanceof Timestamp) {
                    // Appian uses Timestamp for Date Time
                    doc.put(keyName, new Date(((Timestamp) value).getTime()));

                } else if (value instanceof TimestampWithTimezone) {
                    // Sometimes now() returns this datatype. Unfortunately there is no way to get
                    // more accurate than to the minute.
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm a Z");
                    Date parsed = format.parse(value.toString());
                    doc.put(keyName, parsed);

                } else if (value instanceof DateWithTimezone) {
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                    Date parsed = format.parse(value.toString());
                    doc.put(keyName, parsed);

                } else if (value instanceof java.sql.Time) {
                    // There is no good way to handle Time only
                    doc.put(keyName, value.toString());

                } else if (value instanceof Object[]) {
                    // Handle arrays... BSON prefers List<>
                    doc.put(keyName, Arrays.asList((Object[]) value));

                } else {
                    // Was not a datatype with special handling
                    try {
                        // If we can cast it to a Dictionary, recurse
                        typeService.cast(AppianTypeLong.DICTIONARY, dictionary.get(key));
                        doc.put(keyName, typedValueToDocument(typeService, dictionary.get(key)));

                    } catch (Exception e) {
                        // When all else fails, just put
                        doc.put(keyName, value);
                    }
                }
            }
        }

        return doc;
    }

    /**
     * Determines whether the incoming typed value is a Dictionary/CDT or a JSON string.
     * If it's a Dictionary/CDT, converts it to JSON via {@link #typedValueToDocument(TypeService, TypedValue)}.
     * Otherwise, it assumes the value is already JSON and returns it as-is.
     *
     * @param typeService  the Appian TypeService used to interpret typed values
     * @param typedValue   the typed value which might contain a Dictionary/CDT or a JSON string
     * @return a JSON string representing the typed value
     * @throws JAXBException   if an error occurs during XML-to-Java binding
     * @throws ParseException  if an error occurs while parsing date/time values
     */
    public static String getJsonValueFromDictOrString(TypeService typeService, TypedValue typedValue) throws JAXBException, ParseException {
        if (AppianTypeHelper.isListDictOrCdt(typeService, typedValue)) {
            return MongoDbJsonHelper.typedValueToDocument(typeService, typedValue).toJson();
        } else {
            return MongoDbJsonHelper.getJsonValueFromTypedValue(typeService, typedValue, true);
        }
    }


    /**
     * Converts an {@code Object} into a JSON-ready string. Depending on the type, it may wrap
     * values in quotes (for example, Strings) or format them for MongoDB queries (e.g., dates).
     *
     * @param valObject  the object to be converted to a JSON-friendly string
     * @return a string version of the {@code valObject}, safe for JSON usage
     */
    public static String getJsonValueFromObject(Object valObject) {
        return getJsonValueFromObject(valObject, false);
    }


    /**
     * Converts an {@code Object} into a JSON-ready string. Depending on the type, it may wrap
     * values in quotes (for example, Strings) or format them for MongoDB queries (e.g., dates).
     *
     * @param valObject  the object to be converted to a JSON-friendly string
     * @return a string version of the {@code valObject}, safe for JSON usage
     */
    public static String getJsonValueFromObject(Object valObject, Boolean noQuotes) {
        String valString;

        // If we get here with a TypedValue, pull the value out into valObject
        if (valObject instanceof TypedValue) {
            // Special handling for Boolean TypedValues
            if (Objects.equals(((TypedValue) valObject).getInstanceType(), AppianTypeLong.BOOLEAN))
                valObject = ((Long) ((TypedValue) valObject).getValue()) == 1;
            else
                valObject = ((TypedValue) valObject).getValue();
        }

        if (valObject == null || (valObject instanceof String && StringUtils.isEmpty(valObject.toString()))) {
            valString = "null";

        } else if (valObject instanceof Integer || valObject instanceof Long || valObject instanceof Double) {
            valString = valObject.toString();

        } else if (valObject instanceof Boolean) {
            valString = valObject.toString();

        } else if (valObject instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            String text = sdf.format(valObject);
            valString = "ISODate(\"" + text + "\")";

        } else if (valObject instanceof Point) {
            valString = valObject.toString();

        } else if (valObject instanceof Point[]) {
            valString = geoPointArrayToString((Point[]) valObject);

        } else {
            // Strings will match here
            if (noQuotes)
                valString = valObject.toString();
            else
                valString = "\"" + valObject.toString() + "\"";
        }

        return valString;
    }


    /**
     * Extracts the underlying value from an Appian {@link TypedValue} and converts it
     * into a JSON-ready string.
     *
     * @param typeService  the Appian TypeService used to interpret typed values
     * @param value        the typed value to be converted into JSON
     * @return a string that can be used in a JSON expression
     */
    public static String getJsonValueFromTypedValue(TypeService typeService, TypedValue value) {
        return getJsonValueFromTypedValue(typeService, value, false);
    }

    /**
     * Similar to {@link #getJsonValueFromTypedValue(TypeService, TypedValue)}, but offers the option to
     * suppress surrounding quotes for strings.
     *
     * @param typeService  the Appian TypeService used to interpret typed values
     * @param value        the typed value to be converted into JSON
     * @param noQuotes     if {@code true}, strings are returned without surrounding quotes
     * @return a string that can be used in a JSON expression
     */
    public static String getJsonValueFromTypedValue(TypeService typeService, TypedValue value, Boolean noQuotes) {
        if (typeService.getDatatypeProperties(value.getInstanceType()).getName().equals("Boolean")) {
            // Special case for handling booleans. Appian returns them as a Long, 1 if true.
            return value.getValue().equals(1L) ? getJsonValueFromObject(true) : getJsonValueFromObject(false);

        } else if (value.getValue() instanceof String) {
            // Special handling for string values
            if (((String) value.getValue()).matches("^ObjectId\\(\".*\"\\)$")) {
                return value.getValue().toString();
            }
        }

        // No special case matched
        return getJsonValueFromObject(value.getValue(), noQuotes);
    }


    /**
     * Converts an array of {@link TypedValue} objects into a list of JSON-friendly strings.
     *
     * @param typeService  the Appian TypeService used to interpret typed values
     * @param array        an array of typed values to be converted
     * @return a list of string representations that can be used in a JSON expression
     */
    public static List<String> getJsonValuesFromArray(TypeService typeService, TypedValue[] array) {
        List<String> jsonValues = new ArrayList<>();

        if (array.length == 1 &&
                typeService.getDatatypeProperties(array[0].getInstanceType()).getName().matches("^List of .*")) {
            for (Object object : (Object[]) array[0].getValue())
                jsonValues.add(getJsonValueFromObject(object));
        } else {
            for (TypedValue typedValue : array)
                jsonValues.add(getJsonValueFromObject(typedValue.getValue()));
        }

        return jsonValues;
    }


    /**
     * Builds a basic MongoDB operator expression using a given operator (e.g. "$eq") and value,
     * optionally omitting quotes for strings.
     *
     * @param operator  the MongoDB operator (e.g., "$eq", "$gt")
     * @param value     the value to associate with the operator
     * @param noQuotes  if {@code true}, strings are returned without surrounding quotes
     * @return a JSON string representing the operator usage, e.g. {@code { "$eq": "someValue" }}
     */
    public static String buildBasicOperator(String operator, Object value, Boolean noQuotes) {
        return "{ \"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromObject(value, noQuotes) + " }";
    }


    /**
     * Builds a basic MongoDB operator expression using a given operator (e.g. "$eq") and value.
     *
     * @param operator  the MongoDB operator (e.g., "$eq", "$gt")
     * @param value     the value to associate with the operator
     * @return a JSON string representing the operator usage, e.g. {@code { "$eq": "someValue" }}
     */
    public static String buildBasicOperator(String operator, Object value) {
        return "{ \"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromObject(value) + " }";
    }


    /**
     * Builds a basic MongoDB operator expression for a typed value using a given operator.
     * This may also handle nested Dictionary/CDT conversions.
     *
     * @param typeService  the Appian TypeService used to interpret typed values
     * @param operator     the MongoDB operator (e.g., "$eq", "$gt")
     * @param value        the typed value to associate with the operator
     * @return a JSON string representing the operator usage, e.g. {@code { "$eq": "someValue" }}
     * @throws JAXBException   if an error occurs during XML-to-Java binding
     * @throws ParseException  if an error occurs while parsing date/time values
     */
    public static String buildBasicOperator(TypeService typeService, String operator, TypedValue value) throws JAXBException, ParseException {
        return buildBasicOperator(typeService, operator, value, false);
    }


    /**
     * Builds a basic MongoDB operator expression for a typed value using a given operator,
     * optionally omitting quotes for strings. This may also handle nested Dictionary/CDT conversions.
     *
     * @param typeService  the Appian TypeService used to interpret typed values
     * @param operator     the MongoDB operator (e.g., "$eq", "$gt")
     * @param value        the typed value to associate with the operator
     * @param noQuotes     if {@code true}, strings are returned without surrounding quotes
     * @return a JSON string representing the operator usage, e.g. {@code { "$eq": "someValue" }}
     * @throws JAXBException   if an error occurs during XML-to-Java binding
     * @throws ParseException  if an error occurs while parsing date/time values
     */
    public static String buildBasicOperator(TypeService typeService, String operator, TypedValue value, Boolean noQuotes) throws JAXBException, ParseException {
        if (AppianTypeHelper.isListDictOrCdt(typeService, value))
            return "{ \"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromDictOrString(typeService, value) + " }";
        else
            return "{ \"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromObject(value, noQuotes) + " }";
    }


    /**
     * Builds a MongoDB operator expression for an array of JSON values, optionally
     * enclosing each array element in braces.
     *
     * @param operator             the MongoDB operator (e.g. "$and", "$or")
     * @param jsonValues           the list of JSON values to be combined
     * @param encloseEachInBraces  if {@code true}, wraps each element in curly braces
     * @return a JSON string, e.g. {@code { "$and": [ { ... }, { ... } ] }}
     */
    public static String buildArrayOperator(String operator, List<String> jsonValues, Boolean encloseEachInBraces) {
        if (encloseEachInBraces)
            for (int i = 0; i < jsonValues.size(); i++) {
                if (!jsonValues.get(i).matches("^\\s*\\{.*}$"))
                    jsonValues.set(i, "{ " + jsonValues.get(i) + " }");
            }

        return "{ \"" + operator + "\": [ " + String.join(", ", jsonValues) + " ] }";
    }


    /**
     * Converts an array of {@link Point} objects into a single JSON array string.
     *
     * @param array  the array of {@link Point} objects
     * @return a JSON array string representing the points, e.g. {@code [ { "type": "Point", ... }, ... ]}
     */
    public static String geoPointArrayToString(Point[] array) {
        List<String> pointStrings = new ArrayList<>();
        for (Point point : array) {
            pointStrings.add(point.toString());
        }
        return "[ " + String.join(", ", pointStrings) + " ]";
    }


    /**
     * Checks whether the given JSON string is valid by attempting to parse it into a
     * BSON document.
     *
     * @param json  the JSON string to validate
     * @return true if the JSON is valid BSON, false otherwise
     */
    public static boolean isValidJson(String json) {
        try {
            BsonDocument bsonDocument = BsonDocument.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
